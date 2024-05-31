package com.llamacpp.llama

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class Llm {

    private val tag: String? = this::class.simpleName

    private val threadLocalState: ThreadLocal<State> = ThreadLocal.withInitial { State.Idle }

    private val runLoop: CoroutineDispatcher = Executors.newSingleThreadExecutor {
        thread(start = false, name = "Llm-RunLoop") {
            Log.d(tag, "Dedicated thread for native code: ${Thread.currentThread().name}")

            // No-op if called more than once.
            System.loadLibrary("llama-android")

            // Set llama log handler to Android
            logToAndroid()
            backendInit(false)

            Log.d(tag, systemInfo())

            it.run()
        }.apply {
            uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, exception: Throwable ->
                Log.e(tag, "Unhandled exception", exception)
            }
        }
    }.asCoroutineDispatcher()

    private val nlen: Int = 128

    private external fun logToAndroid()
    private external fun loadModel(filename: String): Long
    private external fun freeModel(model: Long)
    private external fun newContext(model: Long): Long
    private external fun freeContext(context: Long)
    private external fun backendInit(numa: Boolean)
    private external fun backendFree()
    private external fun freeBatch(batch: Long)
    private external fun newBatch(nTokens: Int, embd: Int, nSeqMax: Int): Long
    private external fun benchModel(
        context: Long,
        model: Long,
        batch: Long,
        pp: Int,
        tg: Int,
        pl: Int,
        nr: Int
    ): String

    private external fun systemInfo(): String

    private external fun completionInit(
        context: Long,
        batch: Long,
        text: String,
        nLen: Int
    ): Int

    private external fun completionLoop(
        context: Long,
        batch: Long,
        nLen: Int,
        ncur: IntVar
    ): String?

    private external fun kvCacheClear(context: Long)

    suspend fun bench(pp: Int, tg: Int, pl: Int, nr: Int = 1): String {
        return withContext(runLoop) {
            when (val state = threadLocalState.get()) {
                is State.Loaded -> {
                    Log.d(tag, "bench(): $state")
                    benchModel(state.context, state.model, state.batch, pp, tg, pl, nr)
                }

                else -> throw IllegalStateException("No model loaded")
            }
        }
    }

    suspend fun load(pathToModel: String, onLoaded: () -> Unit) {
        withContext(runLoop) {
            when (threadLocalState.get()) {
                is State.Idle -> {
                    val model = loadModel(pathToModel)
                    if (model == 0L)  throw IllegalStateException("load_model() failed")

                    val context = newContext(model)
                    if (context == 0L) throw IllegalStateException("new_context() failed")

                    val batch = newBatch(512, 0, 1)
                    if (batch == 0L) throw IllegalStateException("new_batch() failed")

                    Log.i(tag, "Loaded model $pathToModel")
                    threadLocalState.set(State.Loaded(model, context, batch))
                    onLoaded()
                }
                else -> throw IllegalStateException("Model already loaded")
            }
        }
    }

    suspend fun send(message: String, onMessage: (String) -> Boolean, onEnd: () -> Unit) {
        withContext(runLoop) {
            when (val state = threadLocalState.get()) {
                is State.Loaded -> {
                    val ncur = IntVar(completionInit(state.context, state.batch, message, nlen))
                    while (true) {
                        val str = completionLoop(state.context, state.batch, nlen, ncur) ?: break
                        if (!onMessage(str)) {
                            break
                        }
                    }
                    kvCacheClear(state.context)
                    onEnd()
                }
                else -> {}
            }
        }
    }

    /**
     * Unloads the model and frees resources.
     *
     * This is a no-op if there's no model loaded.
     */
    suspend fun unload() {
        withContext(runLoop) {
            when (val state = threadLocalState.get()) {
                is State.Loaded -> {
                    freeContext(state.context)
                    freeModel(state.model)
                    //freeBatch(state.batch)

                    threadLocalState.set(State.Idle)
                }
                else -> {}
            }
        }
    }

    companion object {
        private class IntVar(value: Int) {
            @Volatile
            var value: Int = value
                private set

            fun inc() {
                synchronized(this) {
                    value += 1
                }
            }
        }

        private sealed interface State {
            data object Idle: State
            data class Loaded(val model: Long, val context: Long, val batch: Long): State
        }

        // Enforce only one instance of Llm.
        private val _instance: Llm = Llm()

        fun instance(): Llm = _instance
    }
}
