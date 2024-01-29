package org.samo_lego.locallm.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.withContext
import org.samo_lego.locallm.config.LMProperties
import org.samo_lego.locallm.lmloader.LMHolder


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Settings() {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Settings")
            })
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
        ) {
            var modelProperties by remember {
                mutableStateOf(
                    LMProperties(
                        "LLM",
                        "/path/to/model.gguf",
                        "You are a well-trained llama.",
                        true,
                    )
                )
            }

            LaunchedEffect(Unit) {
                withContext(coroutineContext) {
                    modelProperties = LMHolder.currentModel().properties
                }
            }

            ModelCard(modelProperties = modelProperties)
        }
    }
}

@Composable
fun ModelCard(modelProperties: LMProperties) {
    var modelName by remember { mutableStateOf(modelProperties.name) }
    var systemPrompt by remember { mutableStateOf(modelProperties.systemPrompt) }
    var useChatML by remember { mutableStateOf(modelProperties.useChatML) }

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = "Model properties", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                label = {
                    Text(text = "Model name")
                },
                value = modelName,
                onValueChange = { value ->
                    modelName = value
                    modelProperties.name = value
                },
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth(),
                label = {
                    Text(text = "System prompt")
                },
                value = systemPrompt,
                onValueChange = { value ->
                    systemPrompt = value
                    modelProperties.systemPrompt = value
                },
            )

            Text(text = "Model Path")
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = modelProperties.modelPath,
                readOnly = true,
                onValueChange = { },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 16.dp),
                ) {
                    Text(text = "Use ChatML")
                }


                Switch(
                    checked = useChatML,
                    onCheckedChange = { value ->
                        useChatML = value
                        modelProperties.useChatML = value
                    },
                )
            }
        }
    }
}