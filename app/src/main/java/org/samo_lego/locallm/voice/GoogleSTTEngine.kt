package org.samo_lego.locallm.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import org.samo_lego.locallm.data.SettingsKeys
import org.samo_lego.locallm.data.appSettings
import java.util.Locale


class GoogleSTTEngine(private val context: Context) : STTEngine {
    private val stt: SpeechRecognizer? = if (SpeechRecognizer.isRecognitionAvailable(context)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(
                context
            ) && appSettings.getBool(
                SettingsKeys.CLOUD_STT,
                false  // todo - once ready, change to true
            )
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
    } else {
        null
    }
    private val intent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

    private var speechReadyEvent: (() -> Unit) = {}
    private var speechBeginEvent: (() -> Unit) = {}
    private var speechEndEvent: (() -> Unit) = {}
    private var rmsChangedEvent: ((Float) -> Unit) = {}
    private var speechResultsEvent: ((String) -> Unit) = {}
    private var errorEvent: ((Int) -> Unit) = {}


    init {
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        //intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            Locale.getDefault()
        )
        stt?.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("STTEngine", "onReadyForSpeech")
                speechReadyEvent()
            }

            override fun onBeginningOfSpeech() {
                Log.d("STTEngine", "onBeginningOfSpeech")
                speechBeginEvent()
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("STTEngine", "onRmsChanged, RMS: $rmsdB")
                rmsChangedEvent(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("STTEngine", "onBufferReceived")
                if (buffer != null) {
                    Log.d("STTEngine", "Buffer word: ${String(buffer)}")
                }
            }

            override fun onEndOfSpeech() {
                Log.d("STTEngine", "onEndOfSpeech")
                speechEndEvent()
            }

            override fun onError(error: Int) {
                Log.d("STTEngine", "onError: $error")
                errorEvent(error)
            }

            override fun onResults(results: Bundle?) {
                Log.d("STTEngine", "onResults")
                if (results != null) {
                    val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (data != null) {
                        Log.d("STTEngine", "Recognized text: ${data[0]}")
                    }
                }

                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.joinToString("")
                if (text != null) {
                    speechResultsEvent(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("STTEngine", "onPartialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("STTEngine", "onEvent: $eventType")
            }
        })
    }

    override fun isAvailable(): Boolean {
        return stt != null
    }

    override fun stopListening() {
        stt?.stopListening()
    }

    override fun startListening(
        onReadyForSpeech: () -> Unit,
        onBeginningOfSpeech: () -> Unit,
        onRmsChanged: (Float) -> Unit,
        onEndOfSpeech: () -> Unit,
        onError: (Int) -> Unit,
        onResults: (String) -> Unit,
    ) {
        speechReadyEvent = onReadyForSpeech
        speechBeginEvent = onBeginningOfSpeech
        rmsChangedEvent = onRmsChanged
        speechEndEvent = onEndOfSpeech
        speechResultsEvent = onResults
        errorEvent = onError


        // todo - make this block until permission is granted / denied
        if (checkMicrophonePermission(context)) {
            stt?.startListening(intent)
        }
    }
}