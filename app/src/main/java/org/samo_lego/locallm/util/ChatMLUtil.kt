package org.samo_lego.locallm.util

import org.samo_lego.locallm.data.LMProperties
import org.samo_lego.locallm.ui.components.message.BotMessage
import org.samo_lego.locallm.ui.components.message.TextResponse
import org.samo_lego.locallm.ui.components.message.UserMessage


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
            return "$conversation\n${im_start}user\n$message$im_end\n${im_start}assistant\n"
        }

        fun toChatML(systemPrompt: String, userPrompt: String) =
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

        fun toText(messages: List<TextResponse>, system: String): String {
            val text = StringBuilder()

            // Add system prompt
            text.append(im_start)
            text.append("system\n")
            text.append(system)
            text.append('\n')
            text.append(im_end)
            text.append('\n')

            for (message in messages) {
                text.append(im_start)
                if (message is UserMessage) {
                    text.append("user")
                } else if (message is BotMessage) {
                    text.append("assistant")
                }
                text.append('\n')
                text.append(message.getText())
                text.append('\n')
                text.append(im_end)
                text.append('\n')
            }

            return text.toString()
        }
    }
}
