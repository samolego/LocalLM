package org.samo_lego.locallm.util

import org.jetbrains.annotations.TestOnly


class ChatMLUtil {
    companion object {
        const val im_end = "<|im_end|>"
        fun toChatML(systemPrompt: String, userPrompt: String) =
            "<|im_start|>system\n$systemPrompt$im_end\n<|im_start|>user\n$userPrompt$im_end\n<|im_start|>assistant\n"

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

    // Unit tests for isPotentialEnd
    @TestOnly
    fun testIsPotentialEnd() {
        assert(isPotentialEnd("Hello <|im_end|>"))
        assert(isPotentialEnd("Hello <|im_end|> world"))
        assert(isPotentialEnd("Hello <im_"))
        assert(!isPotentialEnd("Hello World!"))
    }
}