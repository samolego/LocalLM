package org.samo_lego.locallm.ui.components.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.samo_lego.locallm.data.AvailableModels
import org.samo_lego.locallm.data.LMProperties


@Composable
fun ModelCard(
    modelProperties: LMProperties,
    pathState: MutableState<String> = mutableStateOf(modelProperties.modelPath),
    allowModel: (Uri?) -> Boolean
) {
    var modelName by remember { mutableStateOf(modelProperties.name) }
    var systemPrompt by remember { mutableStateOf(modelProperties.systemPrompt) }
    var useChatML by remember { mutableStateOf(modelProperties.useChatML) }
    var path by remember { pathState }


    // todo - is this needed?
    LaunchedEffect(modelProperties) {
        modelName = modelProperties.name
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

        ModelPathChooser(
            pathState,
            onChoose = { uri ->
                // Set model name if empty
                modelName.ifEmpty {
                    modelName = uri?.lastPathSegment ?: "Unknown"
                    modelProperties.name = modelName
                }

                if (uri != null && uri.path != null && allowModel(uri)) {
                    // Update model path
                    path = uri.path!!
                    modelProperties.modelPath = uri.path!!
                }
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
                    AvailableModels.instance.saveModels()
                },
            )
        }
    }
}


@Composable
fun ModelPathChooser(path: MutableState<String>, onChoose: (Uri?) -> Unit) {
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
        value = path.value,
        label = {
            Text(text = "Model path")
        },
        readOnly = true,
        onValueChange = { },
    )
}

@Composable
fun ModelChooser(selectedItem: String?, onChoose: (LMProperties) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = "Current model", fontWeight = FontWeight.Bold)
        }
        Column {
            Surface(
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            expanded = !expanded
                        },
                ) {
                    Row {
                        Column {
                            Text(
                                text = selectedItem ?: "Select an item",
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .padding(start = 8.dp),
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .align(Alignment.CenterVertically),
                        ) {
                            Icon(
                                if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                "Expand",
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            },
                        ) {
                            AvailableModels.instance.models().forEachIndexed { index, properties ->
                                val top = if (index == 0) {
                                    8.dp
                                } else {
                                    0.dp
                                }

                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = top,
                                        topEnd = top,
                                    )
                                ) {
                                    DropdownMenuItem(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        text = {
                                            Text(
                                                text = properties.name,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        onClick = {
                                            onChoose(properties)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(
                                    bottomStart = 8.dp,
                                    bottomEnd = 8.dp,
                                )
                            ) {
                                DropdownMenuItem(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    text = {
                                        Text(
                                            text = "Add new",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModelChooserPreview() {
    ModelChooser("Test") {}
}
