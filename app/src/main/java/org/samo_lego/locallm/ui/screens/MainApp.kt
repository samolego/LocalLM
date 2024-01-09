package org.samo_lego.locallm.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.withContext
import org.samo_lego.locallm.lmloader.LMHolder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(context: Context) {
    // Set up model in the background
    val modelPath = "${context.filesDir}/tinyllama-2-1b-miniguanaco.Q4_K_M.gguf"

    LaunchedEffect(Unit) {
        withContext(coroutineContext) {
            LMHolder.setModel(modelPath)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LocalLM") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
        ) {
            Conversation()
        }
    }
}
