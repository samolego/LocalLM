package org.samo_lego.locallm.util

import org.samo_lego.locallm.config.LMProperties

fun processUserText(text: String, properties: LMProperties): String {
    // Apply ChatML to text if needed
    if (properties.useChatML) {
        return toChatML(properties.systemPrompt, text)
    }

    return text
}


private fun toChatML(systemPrompt: String, userPrompt: String) =
    """<|im_start|>system
$systemPrompt
<|im_end|>
<|im_start|>user
$userPrompt
<|im_end|>
<|im_start|>assistant
"""
