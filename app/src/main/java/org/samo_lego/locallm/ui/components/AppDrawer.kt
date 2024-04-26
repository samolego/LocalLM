package org.samo_lego.locallm.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.samo_lego.locallm.ui.components.dialog.DeleteConversationDialog
import org.samo_lego.locallm.ui.navigation.Routes
import org.samo_lego.locallm.ui.navigation.navigate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    navController: NavHostController,
    drawerState: DrawerState,
    onNewConversation: () -> Unit,
    availableConversations: List<String>,
    onConversationSelect: (String) -> Unit,
    onDeleteConversation: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val onSecondaryDiff = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f)
    var deleteConversation by remember {
        mutableStateOf("")
    }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row {
                        Button(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            onClick = {
                                // Launch settings
                                navController.navigate(Routes.SETTINGS)

                                // Close drawer
                                coroutineScope.launch {
                                    drawerState.close()
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Settings, "Settings button")
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                text = "Settings",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onNewConversation,
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Add, "Localized description")
                    }
                }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
        ) {
            if (deleteConversation.isNotEmpty()) {
                // Show delete conversation dialog
                DeleteConversationDialog(
                    name = deleteConversation,
                    onCancel = {
                        deleteConversation = ""
                    },
                    onConfirm = {
                        onDeleteConversation(deleteConversation)
                        deleteConversation = ""
                    },
                )
            }

            LazyColumn {
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = "Conversations",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                items(availableConversations.size) { index ->
                    val conversation = availableConversations[index]
                    Row(
                        // Extended to full width
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        onConversationSelect(conversation)
                                    },
                                    onLongClick = {
                                        deleteConversation = conversation
                                    }
                                )
                                .fillMaxWidth()
                                .background(
                                    color = if (index % 2 == 0)
                                        MaterialTheme.colorScheme.onSecondary
                                    else
                                        onSecondaryDiff
                                )
                                .padding(12.dp)
                                .indication(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = rememberRipple()
                                )
                            // Ripple

                        ) {
                            Text(text = conversation)
                        }
                    }
                }
            }
        }
    }
}
