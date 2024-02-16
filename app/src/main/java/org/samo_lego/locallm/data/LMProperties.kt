package org.samo_lego.locallm.data

import kotlinx.serialization.Serializable


const val defaultSystem = "You are a local helpful assistant."

@Serializable
data class LMProperties(
    var name: String,
    var modelPath: String,
    var systemPrompt: String = defaultSystem,
    var useChatML: Boolean = true,
) : Comparable<LMProperties> {
    override fun compareTo(other: LMProperties): Int {
        var comp = name.compareTo(other.name)
        if (comp == 0) {
            comp = modelPath.compareTo(other.modelPath)

            if (comp == 0) {
                comp = systemPrompt.compareTo(other.systemPrompt)
                if (comp == 0) {
                    comp = useChatML.compareTo(other.useChatML)
                }
            }
        }

        return comp
    }
}
