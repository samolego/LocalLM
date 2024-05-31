package org.samo_lego.locallm.lmloader

import com.llamacpp.llama.Llm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.samo_lego.locallm.data.LMProperties
import org.samo_lego.locallm.data.SettingsKeys
import org.samo_lego.locallm.data.appSettings
import org.samo_lego.locallm.ui.screens.modelAvailableState
import org.samo_lego.locallm.ui.screens.modelLoadedState

class LMHolder {
    companion object {
        private var currentModelProperties: LMProperties? = null

        private val suggestQueue = mutableListOf<Suggestion>()
        private var modelLoading: Job? = null

        fun suggest(
            question: String,
            onSuggestion: (String) -> Boolean = { true },
            onEnd: () -> Unit = {},
        ) {
            if (currentModelProperties != null) {
                CoroutineScope(Dispatchers.Default).launch {
                    Llm.instance().send(question, onSuggestion, onEnd)
                }
            } else {
                suggestQueue.add(Suggestion(question, onSuggestion, onEnd))
            }
        }

        private fun onModelLoaded() {
            for (question in suggestQueue) {
                suggest(
                    question.question,
                    question.onSuggestion,
                    question.onEnd,
                )
            }
        }

        suspend fun currentModel(): LMProperties? {
            if (modelLoading != null) {
                modelLoading!!.join()
                modelLoading = null
            }

            return currentModelProperties
        }

        fun setModel(model: LMProperties) {
            if (currentModelProperties != null && currentModelProperties!!.modelPath == model.modelPath) {
                // Just switch the properties
                currentModelProperties = model
                return
            }

            modelLoadedState.value = false
            modelAvailableState.value = true
            currentModelProperties = null

            modelLoading = CoroutineScope(Dispatchers.Default).launch {
                Llm.instance().unload()
                Llm.instance().load(model.modelPath) {
                    currentModelProperties = model
                    appSettings.setString(SettingsKeys.LAST_MODEL, model.name)
                    modelLoadedState.value = true
                    onModelLoaded()
                }
            }
        }
    }
}

private data class Suggestion(
    val question: String,
    val onSuggestion: (String) -> Boolean = { true },
    val onEnd: () -> Unit = {},
)