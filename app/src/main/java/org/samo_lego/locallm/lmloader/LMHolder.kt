package org.samo_lego.locallm.lmloader

import de.kherud.llama.InferenceParameters
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
        private var loadedModel: LoadedModel? = null

        private val suggestQueue = mutableListOf<Suggestion>()
        private var modelLoading: Job? = null

        fun suggest(
            question: String,
            onSuggestion: (String) -> Boolean = { true },
            onEnd: () -> Unit = {},
            inferParams: InferenceParameters = InferenceParameters(),
        ) {
            if (loadedModel != null) {
                CoroutineScope(Dispatchers.Default).launch {
                    loadedModel!!.suggest(question, onSuggestion, onEnd, inferParams)
                }
            } else {
                suggestQueue.add(Suggestion(question, onSuggestion, onEnd, inferParams))
            }
        }

        private fun onModelLoaded() {
            for (question in suggestQueue) {
                suggest(
                    question.question,
                    question.onSuggestion,
                    question.onEnd,
                    question.inferParams
                )
            }
        }

        suspend fun currentModel(): LoadedModel? {
            if (modelLoading != null) {
                modelLoading!!.join()
                modelLoading = null
            }

            return loadedModel
        }

        fun setModel(model: LMProperties) {
            if (loadedModel != null && loadedModel!!.properties.modelPath == model.modelPath) {
                // Just switch the properties
                loadedModel!!.properties = model

                return
            }
            loadedModel?.close()
            modelLoadedState.value = false
            modelAvailableState.value = true
            loadedModel = null

            modelLoading = CoroutineScope(Dispatchers.Default).launch {
                loadedModel = LoadedModel(model)
                appSettings.setString(SettingsKeys.LAST_MODEL, model.name)
                modelLoadedState.value = true
                onModelLoaded()
            }
        }
    }
}

private data class Suggestion(
    val question: String,
    val onSuggestion: (String) -> Boolean = { true },
    val onEnd: () -> Unit = {},
    val inferParams: InferenceParameters = InferenceParameters(),
)