package org.samo_lego.locallm.data

import kotlinx.serialization.Serializable


const val defaultSystem = "You are a local helpful assistant."

@Serializable
data class LMProperties(
    var name: String,
    var modelPath: String,
    var systemPrompt: String = defaultSystem,
    var useChatML: Boolean = true,
)
