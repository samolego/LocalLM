package org.samo_lego.locallm.recorder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import org.samo_lego.locallm.util.encodeWaveFile
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Source: https://github.com/ggerganov/whisper.cpp/blob/master/examples/whisper.android/app/src/main/java/com/whispercppdemo/recorder/Recorder.kt
 */
class Recorder {
    private val scope: CoroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )
    private var recorder: AudioRecordThread? = null

    suspend fun startRecording(
        outputFile: File,
        onError: (Exception) -> Unit,
        onRmsChanged: (Float) -> Unit,
        onStop: () -> Unit
    ) =
        withContext(scope.coroutineContext) {
            recorder = AudioRecordThread(outputFile, onError, onRmsChanged, onStop)
            recorder?.start()
        }

    suspend fun stopRecording() = withContext(scope.coroutineContext) {
        recorder?.stopRecording()
        @Suppress("BlockingMethodInNonBlockingContext")
        recorder?.join()
        recorder = null
    }
}

private class AudioRecordThread(
    private val outputFile: File,
    private val onError: (Exception) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onStop: () -> Unit
) :
    Thread("AudioRecorder") {
    private var quit = AtomicBoolean(false)

    @SuppressLint("MissingPermission")
    override fun run() {
        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ) * 4
            val buffer = ShortArray(bufferSize / 2)

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                16000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            try {
                audioRecord.startRecording()

                val allData = mutableListOf<Short>()

                val start = System.currentTimeMillis()
                while (!quit.get()) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val silenceThreshold = 70
                        var sum = 0
                        var sumSquares = 0.0

                        var silenceCount = 0
                        for (data in buffer) {
                            sum += data
                            val amplitude = abs(data.toFloat() / Short.MAX_VALUE)
                            sumSquares += amplitude * amplitude

                            val rms = (sqrt(amplitude * amplitude) * 1000).coerceIn(1.0f, 20.0f)
                            onRmsChanged(rms)

                            if (amplitude > silenceThreshold) {
                                // Speech detected, reset silence count
                                silenceCount = 0
                            } else {
                                // No speech detected, increment silence count
                                silenceCount++
                            }
                            val currentTime = System.currentTimeMillis()
                            if (silenceCount * 10 > 1000 && currentTime - start > 4000) {
                                // Stop speaking detected
                                quit.set(true)
                            }
                            allData.add(data)
                        }
                    } else {
                        throw java.lang.RuntimeException("audioRecord.read returned $read")
                    }
                }
                audioRecord.stop()
                encodeWaveFile(outputFile, allData.toShortArray())
                onStop()
            } finally {
                audioRecord.release()
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun stopRecording() {
        quit.set(true)
    }
}