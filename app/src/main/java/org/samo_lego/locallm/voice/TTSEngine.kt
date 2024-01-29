package org.samo_lego.locallm.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


private lateinit var ttsEngine: TTSEngine

val tts: TTSEngine
    get() = ttsEngine

class TTSEngine(context: Context) {

    private val sentenceFlow = MutableSharedFlow<String>()
    private val currentSentence = StringBuilder()
    private val tts: TextToSpeech

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                this.language = Locale.getDefault()
                this.speed = 1.0f

                CoroutineScope(Dispatchers.Default).launch {
                    sentenceFlow.asSharedFlow().collect { sentence ->
                        Log.d("TTSEngine", "Received sentence: $sentence")
                        speak(sentence)
                    }
                }
            } else {
                Log.w("LocalLM", "TTS initialization failed with status: $status")
            }
        }
    }

    private suspend fun speak(sentence: String) {
        Log.d("TTSEngine", "Speaking sentence: $sentence")
        tts.awaitSpeak(sentence)
    }

    fun addWord(scope: CoroutineScope, word: String) {
        currentSentence.append(word.replace(mdRegex, ""))
        Log.d("TTSEngine", "Added word: $word. Current sentence: $currentSentence")

        // Check if we have a full sentence
        val andDel = " and "
        val hasMultSentences = currentSentence.contains(andDel)
        if (canSpeak(word)) {
            // Remove markdown for TTS
            val toSpeak = currentSentence.toString()

            if (hasMultSentences) {
                currentSentence.delete(0, currentSentence.indexOf(andDel) + andDel.length)
            } else {
                currentSentence.clear()
            }

            scope.launch {
                sentenceFlow.emit(toSpeak)
            }
        }
    }

    private fun canSpeak(word: String): Boolean {
        return word.last() in sentenceEnd
    }

    private suspend fun TextToSpeech.awaitSpeak(
        text: CharSequence
    ): Unit = suspendCancellableCoroutine { continuation ->
        setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                continuation.resume(Unit)
            }

            @Deprecated(
                "Deprecated in Java", ReplaceWith(
                    "continuation.resumeWithException(RuntimeException(\"Error during TTS. Utterance id: \$utteranceId\"))",
                    "kotlin.coroutines.resumeWithException"
                )
            )
            override fun onError(utteranceId: String?) {
                continuation.resumeWithException(
                    RuntimeException("Error during TTS. Utterance id: $utteranceId")
                )
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                continuation.resumeWithException(
                    RuntimeException("Error during TTS. Error code: $errorCode")
                )
            }
        })

        speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    fun reset() {
        // Clear current sentence
        currentSentence.clear()
        tts.stop()
    }


    var speed: Float = 1.0f
        set(value) {
            tts.setSpeechRate(value)
        }

    var language: Locale = Locale.getDefault()
        set(value) {
            tts.language = value
            field = value
        }

    companion object {
        private val mdRegex = Regex("([#*_~])")
        private val sentenceEnd = setOf('.', '?', '!', ':', ',', '\n')
        fun init(context: Context) {
            if (!::ttsEngine.isInitialized) {
                ttsEngine = TTSEngine(context)
            }
        }
    }
}