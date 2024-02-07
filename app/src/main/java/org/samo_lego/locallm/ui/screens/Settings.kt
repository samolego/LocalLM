package org.samo_lego.locallm.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.samo_lego.locallm.config.LMProperties
import org.samo_lego.locallm.lmloader.LMHolder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Settings")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            // Go back
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
        ) {
            var modelProperties by remember { mutableStateOf<LMProperties?>(null) }

            LaunchedEffect(Unit) {
                modelProperties = LMHolder.currentModel().properties
            }

            ModelCard(modelProperties = modelProperties)
        }
    }
}

@Composable
fun ModelCard(modelProperties: LMProperties?) {
    var modelName by remember { mutableStateOf(modelProperties?.name.orEmpty()) }
    var systemPrompt by remember { mutableStateOf(modelProperties?.systemPrompt.orEmpty()) }
    var useChatML by remember { mutableStateOf(modelProperties?.useChatML ?: false) }

    LaunchedEffect(modelProperties) {
        modelName = modelProperties?.name.orEmpty()
        systemPrompt = modelProperties?.systemPrompt.orEmpty()
        useChatML = modelProperties?.useChatML ?: false
    }

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
                    modelProperties?.name = value
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
                    modelProperties?.systemPrompt = value
                },
            )

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusEvent {
                        // Launch file picker
                        Log.d("LocalLM", "File picker not implemented yet")

                        // De-focus

                    },
                value = modelProperties?.modelPath.orEmpty(),
                label = {
                    Text(text = "Model path")
                },
                enabled = modelProperties?.modelPath == "",
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
                        modelProperties?.useChatML = value
                    },
                )
            }
        }
    }
}