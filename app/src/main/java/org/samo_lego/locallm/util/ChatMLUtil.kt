package org.samo_lego.locallm.util


class ChatMLUtil {
    companion object {
        const val im_end = "<|im_end|>"


        fun toChatML(systemPrompt: String, userPrompt: String) =
            "<|im_start|>system\n$systemPrompt\n$im_end\n<|im_start|>user\n$userPrompt\n$im_end\n<|im_start|>assistant\n"
    }
}