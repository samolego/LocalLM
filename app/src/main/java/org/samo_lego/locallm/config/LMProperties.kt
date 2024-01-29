package org.samo_lego.locallm.config

const val defaultSystem = "You are a local helpful assistant."

data class LMProperties(
    var name: String,
    val modelPath: String,
    var systemPrompt: String = defaultSystem,
    var useChatML: Boolean = false,
)
