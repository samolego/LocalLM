package org.samo_lego.locallm.voice

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

interface STTEngine {
    fun isAvailable(): Boolean
    fun startListening(
        onReadyForSpeech: () -> Unit = {},
        onBeginningOfSpeech: () -> Unit = {},
        onRmsChanged: (Float) -> Unit = {},
        onEndOfSpeech: () -> Unit = {},
        onError: (Int) -> Unit = {},
        onResults: (String) -> Unit = {},
    )

    fun stopListening()
    fun checkMicrophonePermission(context: Context): Boolean {
        val permission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_REQUEST_CODE
            )

            return false
        }
        return true
    }

    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE: Int = 0xA0D10
    }
}