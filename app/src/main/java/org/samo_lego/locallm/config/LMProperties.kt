package org.samo_lego.locallm.config

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


const val defaultSystem = "You are a local helpful assistant."

@Parcelize  // todo
data class LMProperties(
    var name: String,
    val modelPath: String,
    var systemPrompt: String = defaultSystem,
    var useChatML: Boolean = false,
) : Parcelable
