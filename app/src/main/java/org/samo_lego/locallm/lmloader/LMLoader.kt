package org.samo_lego.locallm.lmloader

import android.util.Log
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.LogLevel
import de.kherud.llama.ModelParameters

class LMLoader(modelPath: String, modelParams: ModelParameters = ModelParameters()) :
    AutoCloseable {
    private val model: LlamaModel
    private val path: String = modelPath
    private var isGenerating = false

    init {
        model = LlamaModel(modelPath, modelParams)
    }

    fun suggest(
        text: String,
        inferParams: InferenceParameters = InferenceParameters()
    ): List<String> {
        isGenerating = true
        val suggestions = mutableListOf<String>()
        for (suggestion in model.generate(text, inferParams)) {
            suggestions.add(suggestion.toString())
        }
        isGenerating = false
        return suggestions
    }

    override fun equals(other: Any?): Boolean {
        if (other is LMLoader) {
            return this.path == other.path
        }
        return false
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    override fun toString(): String {
        return "LMLoader(path=$path)"
    }

    override fun close() {
        model.close()
    }


    companion object {
        init {
            val tag = "LocalLM-native"
            LlamaModel.setLogger { level: LogLevel?, message: String? ->
                if (message != null) {
                    when (level) {
                        LogLevel.DEBUG -> Log.d(tag, message)
                        LogLevel.INFO -> Log.i(tag, message)
                        LogLevel.WARN -> Log.w(tag, message)
                        LogLevel.ERROR -> Log.e(tag, message)
                        null -> {}
                    }
                }
            }
        }
    }
}