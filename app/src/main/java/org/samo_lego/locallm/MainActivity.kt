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
import org.samo_lego.locallm.ui.theme.LocalLMTheme
import kotlin.io.path.Path


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val libsDir =
            Path(applicationInfo.nativeLibraryDir).parent.parent.toString() + "base.apk!/lib/arm64-v8a/"
        System.setProperty("de.kherud.llama.lib.path", libsDir)

        //System.loadLibrary("llama")
        //System.loadLibrary("jllama")

        Log.d(TAG, "onCreate: appDataDir = $libsDir")


        setContent {
            LocalLMTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(LMLoader().testText(applicationInfo) + "Android")
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