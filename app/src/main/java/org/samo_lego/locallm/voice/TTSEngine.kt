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
import org.samo_lego.locallm.util.ChatMLUtil
import org.samo_lego.locallm.util.ChatMLUtil.Companion.im_end
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


private lateinit var ttsEngine: TTSEngine

val tts: TTSEngine
    get() = ttsEngine

class TTSEngine(context: Context) {

    private val sentenceFlow = MutableSharedFlow<String>()
    private val currentSentence = StringBuilder()
    private val holdedTokens = StringBuilder()
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
        holdedTokens.append(word)
        if (ChatMLUtil.isPotentialEnd(holdedTokens.toString())) {
            return
        }
        val token = holdedTokens.replace(mdRegex, "")
        holdedTokens.clear()
        // Remove markdown for TTS
        currentSentence.append(token)
        Log.d("TTSEngine", "Added word: $token. Current sentence: $currentSentence")

        // Check if we have a full sentence
        if (canSpeak(token)) {
            val toSpeak = currentSentence.toString().replace(im_end, "")
            currentSentence.clear()

            scope.launch {
                sentenceFlow.emit(toSpeak)
            }
        }
    }

    private fun canSpeak(word: String): Boolean {
        return word.isNotEmpty() && word.last() in sentenceEnd
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
        holdedTokens.clear()
        tts.stop()
    }

    fun finishSentence(ttScope: CoroutineScope) {
        holdedTokens.clear()
        if (currentSentence.isEmpty()) {
            return
        }

        // Speak current sentence
        val toSpeak = currentSentence.toString().replace(im_end, "")
        currentSentence.clear()

        ttScope.launch {
            sentenceFlow.emit(toSpeak)
        }
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
        private val mdRegex = Regex("([#*_~`|])")
        private val sentenceEnd = setOf('.', '?', '!', ':', ',', '\n')
        fun init(context: Context) {
            if (!::ttsEngine.isInitialized) {
                ttsEngine = TTSEngine(context)
            }
        }
    }
}