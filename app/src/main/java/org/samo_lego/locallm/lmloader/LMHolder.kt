package org.samo_lego.locallm.lmloader

import de.kherud.llama.InferenceParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LMHolder {
    companion object {
        private lateinit var loadedModel: LoadedModel

        private val suggestQueue = mutableListOf<Suggestion>()
        private var modelLoading: Job? = null

        fun suggest(
            question: String,
            onSuggestion: (String) -> Unit = {},
            inferParams: InferenceParameters = InferenceParameters(),
        ) {
            if (::loadedModel.isInitialized) {
                CoroutineScope(Dispatchers.Default).launch {
                    loadedModel.suggest(question, onSuggestion, inferParams)
                }
            } else {
                suggestQueue.add(Suggestion(question, onSuggestion, inferParams))
            }
        }

        private fun onModelLoaded() {
            for (question in suggestQueue) {
                suggest(question.question, question.onSuggestion, question.inferParams)
            }
        }


        suspend fun currentModel(): LoadedModel {
            if (modelLoading != null) {
                modelLoading!!.join()
                modelLoading = null
            }

            return loadedModel
        }

        fun setModel(model: String) {
            if (::loadedModel.isInitialized) {
                loadedModel.close()
            }

            modelLoading = CoroutineScope(Dispatchers.Default).launch {
                loadedModel = LoadedModel(model)
                onModelLoaded()
            }
        }
    }
}

private data class Suggestion(
    val question: String,
    val onSuggestion: (String) -> Unit = {},
    val inferParams: InferenceParameters = InferenceParameters(),
)