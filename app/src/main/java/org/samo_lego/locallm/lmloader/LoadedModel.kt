package org.samo_lego.locallm.lmloader

import android.util.Log
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.LogLevel
import de.kherud.llama.ModelParameters
import org.samo_lego.locallm.data.LMProperties

class LoadedModel(
    var properties: LMProperties,
    modelParams: ModelParameters = ModelParameters(),
) :
    AutoCloseable {
    private val model: LlamaModel

    init {
        model = LlamaModel(properties.modelPath, modelParams)
    }

    fun suggest(
        text: String,
        onSuggestion: (String) -> Boolean = { true },
        onEnd: () -> Unit = {},
        inferParams: InferenceParameters = InferenceParameters(),
    ) {
        for (suggestion in model.generate(text, inferParams)) {
            if (!onSuggestion(suggestion.toString())) {
                break
            }
        }

        onEnd()
    }

    override fun equals(other: Any?): Boolean {
        if (other is LoadedModel) {
            return this.properties.modelPath == other.properties.modelPath
        }
        return false
    }

    override fun hashCode(): Int {
        return properties.modelPath.hashCode()
    }

    override fun toString(): String {
        return "LMLoader(path=$properties.modelPath)"
    }

    override fun close() {
        model.close()
    }


    companion object {
        init {
            val tag = "LocalLM-jllama"
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