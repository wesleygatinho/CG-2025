package br.edu.historiaviva

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.edu.historiaviva.ui.App
import br.edu.historiaviva.ui.theme.HistoriaVivaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HistoriaVivaTheme {
                App()
            }
        }
    }
}

