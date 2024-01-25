package org.samo_lego.locallm.config

const val defaultSystem = "You are a local helpful assistant."

data class LMPreferences(
    val systemPrompt: String = defaultSystem,
    val useChatML: Boolean = false,
    val useTTS: Boolean = true
)