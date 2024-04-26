package org.samo_lego.locallm.util

import java.io.File

private const val CONVERSATION_FOLDER = "conversations"

/**
 * Saves conversation to the app's data directory.
 * @param appFilesDir App's files directory.
 * @param fileName Name of the file to save.
 * @param conversationContent Content of the conversation.
 */
fun saveConversation(appFilesDir: String, fileName: String, conversationContent: String) {
    val conversationDir = File(appFilesDir, CONVERSATION_FOLDER)
    if (!conversationDir.exists()) {
        conversationDir.mkdirs()
    }

    val conversationFile = File(conversationDir, "$fileName.txt")
    conversationFile.writeText(conversationContent)
}


/**
 * Loads conversation from the app's data directory.
 */
fun loadConversation(filesDir: String, title: String): String {
    val conversationDir = File(filesDir, CONVERSATION_FOLDER)
    val conversationFile = File(conversationDir, "$title.txt")
    return conversationFile.readText()
}

fun getAvailableConversations(filesDir: String): List<String> {
    val conversationDir = File(filesDir, CONVERSATION_FOLDER)
    return conversationDir.listFiles()?.map { it.nameWithoutExtension } ?: emptyList()
}

fun deleteConversation(filesDir: String, title: String) {
    val conversationDir = File(filesDir, CONVERSATION_FOLDER)
    val conversationFile = File(conversationDir, "$title.txt")
    conversationFile.delete()
}
