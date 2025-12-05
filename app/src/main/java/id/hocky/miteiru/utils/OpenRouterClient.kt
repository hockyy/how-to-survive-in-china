package id.hocky.miteiru.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Minimal client for calling OpenRouter chat completions.
 * Avoids extra dependencies by using HttpURLConnection.
 */
object OpenRouterClient {

    suspend fun requestExplanation(
        apiKey: String,
        model: String,
        sourceLanguage: String,
        sentence: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://openrouter.ai/api/v1/chat/completions")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
            }

            // Build language-specific prompt
            val languageInstruction = when (sourceLanguage.lowercase()) {
                "cantonese" -> """
                    Source language: Cantonese (粵語).
                    When explaining pronunciation, use Jyutping romanization.
                    For each Chinese character, provide the Jyutping reading.
                """.trimIndent()
                "mandarin" -> """
                    Source language: Mandarin Chinese (普通話).
                    When explaining pronunciation, use Pinyin romanization with tone marks.
                """.trimIndent()
                "japanese" -> """
                    Source language: Japanese (日本語).
                    When explaining pronunciation, use Romaji and include Hiragana/Katakana readings.
                """.trimIndent()
                "korean" -> """
                    Source language: Korean (한국어).
                    When explaining pronunciation, use Revised Romanization of Korean.
                """.trimIndent()
                "vietnamese" -> """
                    Source language: Vietnamese (Tiếng Việt).
                    Include tone marks in explanations.
                """.trimIndent()
                else -> "Source language: $sourceLanguage."
            }

            val systemPrompt = """
                You are a translation and explanation assistant.
                $languageInstruction
                
                Translate the given sentence into English and provide:
                1. **Translation**: The English meaning
                2. **Pronunciation**: Romanized reading (using the appropriate system for the language)
                3. **Word breakdown**: Key vocabulary with individual meanings
                4. **Grammar notes**: Brief explanation of any notable grammar structures
                
                Format your response in Markdown for readability.
            """.trimIndent()

            val body = JSONObject().apply {
                put("model", model)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", sentence)
                    })
                })
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = BufferedReader(InputStreamReader(stream)).use { reader ->
                reader.readText()
            }

            if (responseCode !in 200..299) {
                return@withContext Result.failure(RuntimeException("HTTP $responseCode: $responseText"))
            }

            val json = JSONObject(responseText)
            val content = json
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

