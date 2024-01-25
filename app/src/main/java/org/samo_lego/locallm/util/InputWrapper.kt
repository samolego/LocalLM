package org.samo_lego.locallm.util

import org.samo_lego.locallm.lmloader.LMHolder

fun processUserText(text: String): String {
    // Apply ChatML to text if needed
    if (LMHolder.currentModel().preferences.useChatML) {
        return toChatML(LMHolder.currentModel().preferences.systemPrompt, text)
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
