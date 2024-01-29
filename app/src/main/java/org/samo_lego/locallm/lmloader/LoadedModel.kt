package org.samo_lego.locallm.lmloader

import android.util.Log
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.LogLevel
import de.kherud.llama.ModelParameters
import org.samo_lego.locallm.config.LMProperties
import org.samo_lego.locallm.util.processUserText
import java.io.File

class LoadedModel(
    val path: String,
    modelParams: ModelParameters = ModelParameters(),
    var properties: LMProperties = LMProperties(File(path).name, path),
) :
    AutoCloseable {
    private val model: LlamaModel

    init {
        model = LlamaModel(path, modelParams)
    }

    fun suggest(
        text: String,
        onSuggestion: (String) -> Unit = {},
        inferParams: InferenceParameters = InferenceParameters(),
    ) {
        // Pre-process user text
        val procText = processUserText(text, properties)

        for (suggestion in model.generate(procText, inferParams)) {
            onSuggestion(suggestion.toString())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is LoadedModel) {
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