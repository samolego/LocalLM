package org.samo_lego.locallm

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.kherud.llama.InferenceParameters
import de.kherud.llama.LlamaModel
import de.kherud.llama.LogLevel
import de.kherud.llama.ModelParameters
import org.samo_lego.locallm.ui.theme.LocalLMTheme
import kotlin.io.path.Path


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val package_name = "org.samo_lego.locallm"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Navigate 2 dirs up
        System.loadLibrary("llama")
        System.loadLibrary("jllama")
        val libsDir = Path(applicationContext.applicationInfo.nativeLibraryDir).parent.parent.toString() + "base.apk/lib/arm64-v8a/"
        Log.d(TAG, "onCreate: appDataDir = $libsDir")

        System.setProperty("de.kherud.llama.lib.path", libsDir)
        LlamaModel.setLogger { level: LogLevel?, message: String? ->
            print(
                message
            )
        }
        val modelParams = ModelParameters()
            .setNGpuLayers(43)
        val inferParams = InferenceParameters()
            .setTemperature(0.7f)
            .setPenalizeNl(true)
            .setMirostat(InferenceParameters.MiroStat.V2)
            .setAntiPrompt("\n")

        val modelPath =
            "/home/samoh/Downloads/llama.cpp/models/7B/dolphin-2_6-phi-2.Q5_K_M.gguf"
        val system = """
            This is a conversation between User and Llama, a friendly chatbot.
            Llama is helpful, kind, honest, good at writing, and never fails to answer any requests immediately and with precision.
            
            """.trimIndent()

        var out = "Phi:"
        LlamaModel(modelPath, modelParams).use { model ->
            print(system)
            var prompt: String? = system
            prompt += "\nUser: "
            print("\nUser: ")
            val input: String = """
                <|im_start|>system
                You are a friendly chatbot DolPHIn.<|im_end|>
                <|im_start|>user
                What is android application?<|im_end|>
                <|im_start|>assistant
                """.trimIndent()
            prompt += input
            print("Llama: ")
            prompt += "\nLlama: "
            for (output in model.generate(prompt, inferParams)) {
                out = output.toString()
                prompt += output
            }
        }


        setContent {
            LocalLMTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(out)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LocalLMTheme {
        Greeting("Android")
    }
}