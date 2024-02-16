package org.samo_lego.locallm.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ModelCopyDialog(
    onCancel: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var deleteFileAfterCopy by remember { mutableStateOf(false) }

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
                    onCancel()
                }
            ) {
                Text("Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(deleteFileAfterCopy)
                }
            ) {
                Text("Ok")
            }
        },
    )
}
