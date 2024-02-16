package org.samo_lego.locallm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import org.samo_lego.locallm.data.AppSettings
import org.samo_lego.locallm.data.AvailableModels
import org.samo_lego.locallm.ui.screens.MainApp
import org.samo_lego.locallm.ui.theme.LocalLMTheme
import org.samo_lego.locallm.voice.TTSEngine


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSettings.init { getSharedPreferences(it, MODE_PRIVATE) }
        TTSEngine.init(this)
        AvailableModels.init(filesDir.absolutePath)

        setContent {
            LocalLMTheme {
                MainApp()
            }
        }
    }


    fun requestPermissionLauncher(fn: (Boolean) -> Unit) {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            fn(it)
        }
    }
}
