package org.samo_lego.locallm.lmloader

import de.kherud.llama.InferenceParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class LMHolder {
    companion object {
        private lateinit var loadedModel: LoadedModel

        private val suggestQueue = mutableListOf<Suggestion>()
        private var switchingModel: AtomicBoolean = AtomicBoolean(false)

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

        fun setModel(model: String) {
            if (::loadedModel.isInitialized) {
                loadedModel.close()
            }
            switchingModel = AtomicBoolean(true)

            CoroutineScope(Dispatchers.Default).launch {
                loadedModel = LoadedModel(model)
                switchingModel = AtomicBoolean(false)
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