package org.samo_lego.locallm.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import org.samo_lego.locallm.data.LMProperties
import org.samo_lego.locallm.ui.components.dialog.ModelCopyDialog
import org.samo_lego.locallm.ui.components.settings.ModelCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewModelScreen(navController: NavHostController?) {
    val lmProperties = LMProperties(name = "", modelPath = "")
    var showModelCopyDialog by remember { mutableStateOf(false) }
    var uri by remember { mutableStateOf<Uri?>(null) }
    var deleteFile = false

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Add model")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
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
            if (showModelCopyDialog) {
                ModelCopyDialog(
                    onCancel = {
                        uri = null
                        showModelCopyDialog = false
                    },
                    onConfirm = {
                        showModelCopyDialog = false
                        deleteFile = it
                    },
                )
            }

            Row {
                ModelCard(
                    pathState = mutableStateOf(uri?.path ?: ""),
                    modelProperties = lmProperties,
                    allowModel = { selectedUri ->
                        if (selectedUri != null) {
                            uri = selectedUri
                            showModelCopyDialog = true

                            return@ModelCard true
                        }
                        return@ModelCard false
                    },
                )
            }

            Row(
                horizontalArrangement = Arrangement.Absolute.Center,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Button(
                    enabled = uri != null,
                    onClick = {
                        navController?.popBackStack()
                        // Add to available models
                        uri?.let { modelUri ->
                            prepareAndRunModel(
                                context,
                                modelUri,
                                lmProperties,
                                deleteFile
                            )
                        }
                    }
                ) {
                    Row {
                        Text("Load Model")
                    }
                    Row {
                        Icon(Icons.Default.PlayArrow, "Load model")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun NewModelScreenPreview() {
    NewModelScreen(null)
}
