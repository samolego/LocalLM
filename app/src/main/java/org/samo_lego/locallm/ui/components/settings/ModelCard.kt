package org.samo_lego.locallm.ui.components.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.samo_lego.locallm.data.AvailableModels
import org.samo_lego.locallm.data.LMProperties


@Composable
fun ModelCard(modelProperties: LMProperties, onChooseModel: (Uri?) -> Unit) {
    var systemPrompt by remember { mutableStateOf(modelProperties.systemPrompt) }
    var useChatML by remember { mutableStateOf(modelProperties.useChatML) }
    val path by remember { mutableStateOf(modelProperties.modelPath) }


    // todo - is this needed?
    LaunchedEffect(modelProperties) {
        systemPrompt = modelProperties.systemPrompt
        useChatML = modelProperties.useChatML
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Text(text = "Model properties", fontWeight = FontWeight.Bold)

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

        ModelPathChooser(
            path,
            onChoose = { uri ->
                // Set model name if empty
                if (modelProperties.name.isEmpty()) {
                    modelProperties.name = uri?.lastPathSegment ?: "Unknown"
                }
                onChooseModel(uri)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
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


@Composable
fun ModelPathChooser(path: String, onChoose: (Uri?) -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { docUri ->
            onChoose(docUri)
        }

    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusEvent { focus ->
                if (focus.hasFocus) {
                    launcher.launch(arrayOf("*/*"))
                    focusManager.clearFocus()
                }
            }
            .clickable {
                // Launch file picker
                launcher.launch(arrayOf("*/*"))
            },
        value = path,
        label = {
            Text(text = "Model path")
        },
        enabled = path == "",
        readOnly = true,
        onValueChange = { },
    )
}

@Composable
fun ModelChooser(selectedItem: String?, onChoose: (LMProperties) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Text(text = "Current model", fontWeight = FontWeight.Bold)
    Box(
        modifier = Modifier
            .clickable {
                expanded = !expanded
            }
            .fillMaxWidth(),
    ) {
        Text(
            text = selectedItem ?: "Select an item",
            modifier = Modifier.padding(16.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
        ) {
            AvailableModels.instance.models().forEach { (name, properties) ->
                DropdownMenuItem(
                    text = {
                        Text(text = name, style = MaterialTheme.typography.bodyMedium)
                    },
                    onClick = {
                        onChoose(properties)
                        expanded = false
                    }
                )
            }

        }
    }
}
