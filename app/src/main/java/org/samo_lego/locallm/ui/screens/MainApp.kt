package org.samo_lego.locallm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.samo_lego.locallm.data.AvailableModels
import org.samo_lego.locallm.data.SettingsKeys
import org.samo_lego.locallm.data.appSettings
import org.samo_lego.locallm.data.defaultSystem
import org.samo_lego.locallm.lmloader.LMHolder
import org.samo_lego.locallm.ui.components.AppDrawer
import org.samo_lego.locallm.ui.components.dialog.SaveConversationDialog
import org.samo_lego.locallm.ui.components.message.TextResponse
import org.samo_lego.locallm.ui.navigation.Routes
import org.samo_lego.locallm.ui.navigation.navigate
import org.samo_lego.locallm.util.ChatMLUtil
import org.samo_lego.locallm.util.deleteConversation
import org.samo_lego.locallm.util.getAvailableConversations
import org.samo_lego.locallm.util.loadConversation
import org.samo_lego.locallm.util.saveConversation


const val appTitle = "LocalLM"

@Composable
fun MainApp(filesDir: String) {
    AppView(filesDir)
}

val modelLoadedState = mutableStateOf(false)
val modelAvailableState = mutableStateOf(false)

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AppView(filesDir: String = "") {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    var moreOptionsExpanded by remember { mutableStateOf(false) }
    var showSaveConversationDialog by remember { mutableStateOf(false) }

    // Information about the model
    val modelLoaded by remember { modelLoadedState }
    val modelAvailable by remember { modelAvailableState }

    // The current message being created
    val currentMessage = remember { mutableStateOf("") }

    // The messages in the conversation
    val messages = remember { mutableStateListOf<TextResponse>() }

    var currentConversationName by remember { mutableStateOf("") }

    // Available conversations
    val availableConversations = remember {
        val saved = getAvailableConversations(filesDir)
        mutableStateListOf<String>().apply {
            for (msg in saved) {
                add(msg)
            }
        }
    }


    // Set up model in the background
    LaunchedEffect(Unit) {
        withContext(coroutineContext) {
            appSettings.getString(SettingsKeys.LAST_MODEL, "").let { modelName ->
                if (modelName.isNotEmpty()) {
                    AvailableModels.instance.getModel(modelName)?.let { model ->
                        modelAvailableState.value = true
                        // Launch coroutine to load model
                        LMHolder.setModel(model)
                    }
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(300.dp)
                    .background(Color.Transparent),
            ) {
                AppDrawer(
                    navController,
                    drawerState,
                    onNewConversation = {
                        // Clear current conversation
                        messages.clear()
                        currentMessage.value = ""
                        currentConversationName = ""

                        // Close drawer
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    },
                    availableConversations = availableConversations,
                    onConversationSelect = { title ->
                        // Load conversation
                        currentMessage.value = loadConversation(filesDir, title)
                        val newMessages = loadMessages(currentMessage.value).toList()
                        messages.clear()
                        for (msg in newMessages) {
                            messages.add(msg)
                        }
                        currentConversationName = title
                    },
                    onDeleteConversation = { title ->
                        // Delete conversation
                        deleteConversation(filesDir, title)

                        // Remove from available conversations
                        availableConversations.remove(title)
                    }
                )
            }
        },
        drawerState = drawerState,
    ) {
        NavHost(navController = navController, startDestination = Routes.HOME.path) {
            composable(Routes.SETTINGS.path) { Settings(navController) }
            composable(Routes.ADD_MODEL.path) { NewModelScreen(navController) }
            composable(Routes.HOME.path) {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = {
                                Text(
                                    text = appTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            drawerState.open()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = "Localized description"
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    // Show more options
                                    moreOptionsExpanded = !moreOptionsExpanded

                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.MoreVert,
                                        contentDescription = "Localized description"
                                    )
                                }

                                DropdownMenu(
                                    expanded = moreOptionsExpanded,
                                    onDismissRequest = { moreOptionsExpanded = false },
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.Start,
                                                ) {
                                                    Text("Save")
                                                }
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.SaveAs,
                                                        contentDescription = "Save",
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            moreOptionsExpanded = false
                                            showSaveConversationDialog =
                                                currentConversationName.isEmpty()

                                            if (!showSaveConversationDialog) {
                                                saveCurrentConversation(
                                                    coroutineScope,
                                                    currentConversationName,
                                                    messages,
                                                    filesDir,
                                                )
                                            }
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.Start,
                                                ) {
                                                    Text("Clear")
                                                }
                                                Column(
                                                    horizontalAlignment = Alignment.End,
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Clear",
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            moreOptionsExpanded = false

                                            messages.clear()
                                            currentMessage.value = ""
                                        },
                                    )
                                }
                            },
                        )
                    }
                ) { paddingValues ->
                    if (showSaveConversationDialog) {
                        SaveConversationDialog(
                            onCancel = {
                                showSaveConversationDialog = false
                            },
                            onConfirm = { conversationName ->
                                currentConversationName = conversationName
                                showSaveConversationDialog = false

                                saveCurrentConversation(
                                    coroutineScope,
                                    currentConversationName,
                                    messages,
                                    filesDir,
                                )

                                // Add conversation to available conversations
                                availableConversations.add(currentConversationName)
                            }
                        )
                    }

                    if (modelLoaded) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Conversation(
                                messages = messages,
                                currentMessage = currentMessage,
                                onMessageGenerated = {
                                    if (currentConversationName.isNotEmpty()) {
                                        saveCurrentConversation(
                                            coroutineScope,
                                            currentConversationName,
                                            messages,
                                            filesDir,
                                        )
                                    }
                                }
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            if (modelAvailable) {
                                Text("Waiting for model to load ...")

                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .padding(16.dp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            } else {
                                Row {
                                    Text("No models found.")
                                }
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            navController.navigate(Routes.ADD_MODEL)
                                        },
                                    ) {
                                        Icon(Icons.Default.Add, "Add model")
                                        Text(text = "Add model")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun loadMessages(messagesText: String): MutableList<TextResponse> {
    return TextResponse.fromText(messagesText, ChatMLUtil.im_start)
}

fun saveCurrentConversation(
    coroutineScope: CoroutineScope,
    conversationName: String,
    messages: List<TextResponse>,
    filesDir: String,
) {
    if (conversationName.isNotEmpty()) {
        coroutineScope.launch {
            // Get current system prompt
            var currentPrompt =
                LMHolder.currentModel()?.systemPrompt
            if (currentPrompt == null) {
                currentPrompt = defaultSystem
            }

            // Convert messages to text
            val conversation = ChatMLUtil.toText(messages, currentPrompt)
            saveConversation(
                filesDir,
                conversationName,
                conversation,
            )
        }
    }
}