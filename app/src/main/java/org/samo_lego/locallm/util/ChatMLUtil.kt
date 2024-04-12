package org.samo_lego.locallm.util

import org.samo_lego.locallm.data.LMProperties


class ChatMLUtil {
    companion object {
        const val im_end = "<|im_end|>"
        const val im_start = "<|im_start|>"

        fun processUserText(text: String, properties: LMProperties): String {
            // Apply ChatML to text if needed
            if (properties.useChatML) {
                return toChatML(properties.systemPrompt, text)
            }

            return text
        }

        fun format(properties: LMProperties, userMessages: List<String>, assistantMessages: List<String>): String {
            val sb = StringBuilder()
            sb.append("${im_start}system\n${properties.systemPrompt}$im_end\n")
            for (i in userMessages.indices) {
                sb.append("${im_start}user\n${userMessages[i]}$im_end\n")
                sb.append("${im_start}assistant\n${assistantMessages[i]}$im_end\n")
            }
            return sb.toString()
        }

        fun addUserMessage(conversation: String, message: String): String {
            return "$conversation${im_start}user\n$message$im_end\n"
        }

        private fun toChatML(systemPrompt: String, userPrompt: String) =
            "${im_start}system\n$systemPrompt$im_end\n${im_start}user\n$userPrompt$im_end\n${im_start}assistant\n"

        /**
         * Checks whether sentence has potential chatml ending.
         * Examples:
         * - "Hello <|im_end|>" -> true
         * - "Hello <|im_end|> world" -> true
         * - "Hello <im_" -> true
         * - "Hello World!" -> false
         */
        fun isPotentialEnd(token: String): Boolean {
            var ix = 0
            for (c in token) {
                if (ix < im_end.length && c == im_end[ix]) {
                    ix++
                } else if (ix == im_end.length) {
                    return true
                } else if (ix != 0) {
                    return false
                }
            }

            return ix != 0
        }
    }
}
