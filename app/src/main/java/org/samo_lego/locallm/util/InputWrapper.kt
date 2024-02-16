package org.samo_lego.locallm.util

import org.samo_lego.locallm.data.LMProperties
import org.samo_lego.locallm.util.ChatMLUtil.Companion.toChatML

fun processUserText(text: String, properties: LMProperties): String {
    // Apply ChatML to text if needed
    if (properties.useChatML) {
        return toChatML(properties.systemPrompt, text)
    }

    return text
}
