@file:OptIn(ExperimentalMaterial3Api::class)

package org.samo_lego.locallm.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.samo_lego.locallm.data.AvailableModels
import org.samo_lego.locallm.data.LMProperties
import org.samo_lego.locallm.lmloader.LMHolder
import org.samo_lego.locallm.ui.components.settings.ModelCard
import org.samo_lego.locallm.ui.components.settings.ModelChooser
import java.io.File
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    var showFileDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    var modelUri = Uri.EMPTY
    var deleteFileAfterCopy by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
        ) {

            if (showFileDialog) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text("Copy model")
                    },
                    text = {
                        Column {
                            Row {
                                Text("Model will be copied to the app's data directory. Continue?")
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        deleteFileAfterCopy = !deleteFileAfterCopy
                                    }
                            ) {
                                // Delete model checkbox
                                Checkbox(
                                    checked = deleteFileAfterCopy,
                                    onCheckedChange = {
                                        deleteFileAfterCopy = it
                                    },
                                )
                                Text("Delete original file")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showFileDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showFileDialog = false

                                coroutineScope.launch {
                                    withContext(coroutineScope.coroutineContext) {
                                        prepareAndRunModel(
                                            context,
                                            modelUri,
                                            modelProperties,
                                            deleteFileAfterCopy
                                        )
                                    }
                                }
                            }
                        ) {
                            Text("Ok")
                        }
                    },
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
                onChooseModel = { uri ->
                    if (uri != null) {
                        modelUri = uri
                        showFileDialog = true
                    }
                }
            )


            // Model chooser
            ModelChooser(
                selectedItem = modelProperties.name,
                onChoose = { properties ->
                    modelProperties = properties
                }
            )
        }
    }
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
