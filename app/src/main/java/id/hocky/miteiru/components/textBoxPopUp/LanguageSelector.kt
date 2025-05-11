package id.hocky.miteiru.components.textBoxPopUp

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

@Composable
fun LanguageSelector(
    selectedLanguage: MiteiruProcess,
    onLanguageSelected: (MiteiruProcess) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedButton(
            onClick = { onLanguageSelected(MiteiruProcess.MANDARIN) },
            modifier = Modifier.padding(horizontal = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selectedLanguage == MiteiruProcess.MANDARIN)
                    Color(0xFFE0E0E0) else Color.Transparent
            )
        ) {
            Text("Mandarin")
        }

        OutlinedButton(
            onClick = { onLanguageSelected(MiteiruProcess.CANTONESE) },
            modifier = Modifier.padding(horizontal = 4.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (selectedLanguage == MiteiruProcess.CANTONESE)
                    Color(0xFFE0E0E0) else Color.Transparent
            )
        ) {
            Text("Cantonese")
        }
    }
}