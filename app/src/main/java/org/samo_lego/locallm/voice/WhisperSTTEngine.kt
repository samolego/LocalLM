package org.samo_lego.locallm.voice

import android.content.res.AssetManager
import android.util.Log
import com.whispercpp.whisper.WhisperContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.samo_lego.locallm.recorder.Recorder
import org.samo_lego.locallm.util.decodeWaveFile
import java.io.File
import java.util.concurrent.Executors

class WhisperSTTEngine(assets: AssetManager) : STTEngine {
    private var canTranscribe: Boolean = true
    private var isRecording: Boolean = false
    private var recorder: Recorder = Recorder()
    private var recordedFile: File? = null
    private var whisperContext: WhisperContext? = null
    private val coroutineScope: CoroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )

    init {
        Log.d(LOG_TAG, "Loading whisper model...\n")
        val models = assets.list("models/")
        if (models != null) {
            whisperContext = WhisperContext.createContextFromAsset(
                assets,
                "models/" + models[0]
            )
            Log.i(LOG_TAG, "Loaded model ${models[0]}.\n")
        }
    }

    override fun isAvailable(): Boolean {
        return true
    }

    override fun startListening(
        onReadyForSpeech: () -> Unit,
        onBeginningOfSpeech: () -> Unit,
        onRmsChanged: (Float) -> Unit,
        onEndOfSpeech: () -> Unit,
        onError: (Int) -> Unit,
        onResults: (String) -> Unit
    ) {
        if (isRecording) {
            return
        }

        coroutineScope.launch {
            try {
                Log.i(LOG_TAG, "Recording started")
                val file = getTempFileForRecording()
                onReadyForSpeech()
                onBeginningOfSpeech()
                recorder.startRecording(file,
                    onError = {
                        coroutineScope.launch {
                            withContext(Dispatchers.Main) {
                                Log.d(LOG_TAG, "Error while recording")
                                isRecording = false
                            }
                        }
                    },
                    onRmsChanged = onRmsChanged,
                    onStop = {
                        Log.i(LOG_TAG, "Recording stopped")
                        onEndOfSpeech()
                        stopListening()
                        coroutineScope.launch {
                            recordedFile?.let {
                                val text = transcribeAudio(it)
                                if (text != null) {
                                    onResults(text)
                                }
                            }
                        }
                    }
                )
                isRecording = true
                recordedFile = file
            } catch (e: Exception) {
                Log.w(LOG_TAG, e)
                isRecording = false
            }
        }
    }


    private suspend fun transcribeAudio(file: File): String? {
        if (!canTranscribe) {
            return null
        }

        canTranscribe = false

        var text: String? = null
        try {
            Log.i(LOG_TAG, "Reading wave samples... ")
            val data = decodeWaveFile(file)
            Log.d(LOG_TAG, "${data.size / (16_000 / 1000)} ms\n")
            Log.d(LOG_TAG, "Transcribing data...\n")
            val start = System.currentTimeMillis()
            text = whisperContext?.transcribeData(data)
            val elapsed = System.currentTimeMillis() - start

            Log.d(LOG_TAG, "Done ($elapsed ms): $text\n")

        } catch (e: Exception) {
            Log.w(LOG_TAG, e)
        }

        canTranscribe = true

        return text
    }

    override fun stopListening() {
        coroutineScope.launch {
            recorder.stopRecording()
            isRecording = false
            recordedFile?.let { transcribeAudio(it) }
        }
    }

    private suspend fun getTempFileForRecording() = withContext(Dispatchers.IO) {
        File.createTempFile("recording", "wav")
    }

    companion object {
        private const val LOG_TAG = "WhisperSTTEngine"
    }
}