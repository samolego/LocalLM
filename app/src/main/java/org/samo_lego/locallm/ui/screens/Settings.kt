@file:OptIn(ExperimentalMaterial3Api::class)

package org.samo_lego.locallm.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.samo_lego.locallm.data.AvailableModels
import org.samo_lego.locallm.data.LMProperties
import org.samo_lego.locallm.lmloader.LMHolder
import org.samo_lego.locallm.ui.components.settings.ModelCard
import org.samo_lego.locallm.ui.components.settings.ModelChooser
import org.samo_lego.locallm.ui.components.settings.ModelCopyDialog
import java.io.File
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavHostController?) {
    val coroutineScope = rememberCoroutineScope()
    var showFileDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var modelUri = Uri.EMPTY

    var modelProperties by remember {
        mutableStateOf(
            LMProperties(
                name = "",
                modelPath = ""
            )
        )
    }


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
                            AvailableModels.instance.saveModels()
                            // Go back
                            navController?.popBackStack()
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
        ) {

            if (showFileDialog) {
                ModelCopyDialog(
                    onCancel = {
                        showFileDialog = false
                    },
                    onConfirm = { deleteAfterCopy ->
                        showFileDialog = false

                        coroutineScope.launch {
                            prepareAndRunModel(
                                context,
                                modelUri,
                                modelProperties,
                                deleteAfterCopy
                            )
                        }
                    }
                )
            }
            LaunchedEffect(Unit) {
                val props = LMHolder.currentModel()?.properties
                if (props != null) {
                    modelProperties = props
                }
            }

            ModelCard(
                modelProperties = modelProperties,
                allowModel = { uri ->
                    if (uri != null) {
                        modelUri = uri
                        showFileDialog = true

                        AvailableModels.instance.saveModels()
                        return@ModelCard true
                    }

                    return@ModelCard false
                }
            )


            // Model chooser
            ModelChooser(
                selectedItem = modelProperties.name,
                onChoose = { properties ->
                    modelProperties = properties
                    LMHolder.setModel(properties)
                }
            )
        }
    }
}

@Preview
@Composable
fun SettingsPreview() {
    Settings(null)
}

fun prepareAndRunModel(
    context: Context,
    uri: Uri,
    modelProperties: LMProperties,
    deleteFileAfterCopy: Boolean
) {
    var name = DocumentsContract.getDocumentId(uri).split("/").lastOrNull()
        ?: UUID.randomUUID().toString()

    if (!name.endsWith(".gguf")) {
        name += ".gguf"
    }

    val modelsFolder = AvailableModels.getModelsFolder(context)
    if (!modelsFolder.exists()) {
        modelsFolder.mkdirs()
    }
    val outputFile = File(modelsFolder, name)
    val contentResolver = context.contentResolver

    if (!outputFile.exists()) {
        contentResolver.openInputStream(uri)?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    if (deleteFileAfterCopy) {
        DocumentsContract.deleteDocument(contentResolver, uri)
    }

    // Update model path
    modelProperties.modelPath = outputFile.toString()
    AvailableModels.instance.addModel(modelProperties)

    // Run model
    LMHolder.setModel(modelProperties)
}
