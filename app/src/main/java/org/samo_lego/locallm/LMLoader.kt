package org.samo_lego.locallm

import android.content.pm.ApplicationInfo
import android.util.Log
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.LogLevel
import de.kherud.llama.ModelParameters
import kotlin.io.path.Path

class LMLoader {
    fun testText(applicationInfo: ApplicationInfo): String {
        LlamaModel.setLogger { level: LogLevel?, message: String? ->
            Log.d(
                "Llama",
                """
                    $level: $message
                    """.trimIndent()
            )
        }
        val modelParams = ModelParameters()
        val inferParams = InferenceParameters()
            .setTemperature(0.7f)
            .setPenalizeNl(true)
            .setNPredict(10)
            .setMirostat(InferenceParameters.MiroStat.V2)
            .setAntiPrompt("\n")

        val modelPath =
            Path(applicationInfo.nativeLibraryDir).parent.parent.toString() + "/phi2q5.gguf"
        val system = """
            This is a conversation between User and Llama, a friendly chatbot.
            Llama is helpful, kind, honest, good at writing, and never fails to answer any requests immediately and with precision.
            
            """.trimIndent()

        var out = "Llama:"
        LlamaModel(modelPath, modelParams).use { model ->
            Log.d("Llama", system)
            var prompt: String? = system
            prompt += "\nUser: "
            Log.d("Llama", "\nUser: ")
            val input: String = "What is Android?".trimIndent()
            prompt += input
            Log.d("Llama", "Llama: ")
            prompt += "\nLlama: "
            for (output in model.generate(prompt, inferParams)) {
                out = output.toString()
                prompt += output
            }
            Log.d("Llama", "testText: $out")
        }

        return out
    }
}