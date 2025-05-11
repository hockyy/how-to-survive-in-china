package id.hocky.miteiru.components.textBoxPopUp

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.hocky.miteiru.utils.language.MiteiruProcess
import android.content.Context

@Composable
fun LanguageSelector(
    selectedLanguage: MiteiruProcess,
    onLanguageSelected: (MiteiruProcess) -> Unit,
    text: String,
    context: Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {

        OutlinedButton(
            onClick = {
                // Launch another app
                val intent = Intent().apply {
                    // For a specific app, use its package name
                    // For example, to open Pleco (Chinese dictionary)
                    setPackage("com.embermitre.hanping.app.pro") // Replace with actual package name

                    // If you want to send the text to that app
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text) // You'll need to pass textBox to this component
                    type = "text/plain"

                    // Alternative: use a custom action if the app supports it
                    // action = "com.example.app.SEARCH"
                    // putExtra("query", textBox.text)
                }

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Could not open the app",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.padding(horizontal = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selectedLanguage == MiteiruProcess.CHINESE)
                    Color(0xFFE0E0E0) else Color.Transparent
            )
        ) {
            Text("Mandarin") // Or keep it as "Cantonese" if you prefer
        }

        OutlinedButton(
            onClick = {
                // Launch another app
                val intent = Intent().apply {
                    // For a specific app, use its package name
                    // For example, to open Pleco (Chinese dictionary)
                    setPackage("com.embermitre.hanping.cantodict.app.pro") // Replace with actual package name

                    // If you want to send the text to that app
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text) // You'll need to pass textBox to this component
                    type = "text/plain"

                    // Alternative: use a custom action if the app supports it
                    // action = "com.example.app.SEARCH"
                    // putExtra("query", textBox.text)
                }

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Could not open the app",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            },
            modifier = Modifier.padding(horizontal = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selectedLanguage == MiteiruProcess.CHINESE)
                    Color(0xFFE0E0E0) else Color.Transparent
            )
        ) {
            Text("Cantonese") // Or keep it as "Cantonese" if you prefer
        }
    }
}