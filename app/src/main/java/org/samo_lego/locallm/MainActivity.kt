package org.samo_lego.locallm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.samo_lego.locallm.ui.screens.MainApp
import org.samo_lego.locallm.ui.theme.LocalLMTheme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalLMTheme {
                MainApp(applicationContext)
            }
        }
    }
}
