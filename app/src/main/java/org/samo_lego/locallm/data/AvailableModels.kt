package org.samo_lego.locallm.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.TreeMap


@Serializable
class AvailableModels {
    private val models: MutableMap<String, LMProperties> = TreeMap()

    fun addModel(properties: LMProperties) {
        models[properties.name] = properties
        saveModels()
    }

    fun getModel(name: String): LMProperties? {
        return models[name]
    }

    fun models(): Map<String, LMProperties> {
        return models
    }

    fun removeModel(name: String) {
        models.remove(name)
        saveModels()
    }


    /**
     * Loads available models from the file.
     */
    fun loadModels() {
        val file = File(appPath, filename)
        if (!file.exists()) {
            file.createNewFile()
            this.saveModels()
        } else {
            // Read file
            val content = file.readText(StandardCharsets.UTF_8)
            val availableModels = Json.decodeFromString(content) as AvailableModels
            models.putAll(availableModels.models)
        }
    }

    fun saveModels() {
        val file = File(appPath, filename)
        var write = false
        if (!file.exists()) {
            try {
                file.createNewFile()
                write = true
            } catch (_: Exception) {
                Log.e("AvailableModels", "Failed to create file")
            }
        }

        if (write) {
            file.writeText(this.toString())
        }
    }

    override fun toString(): String {
        return Json.encodeToString(this)
    }

    companion object {
        val instance = AvailableModels()
        private var appPath = "."
        const val filename = "models.json"

        fun init(appPath: String) {
            AvailableModels.appPath = appPath
            instance.loadModels()
            Log.d("AvailableModels", "Models loaded: ${instance.models.size}")
        }


        fun getModelsFolder(context: Context): File {
            val dataDir = File(context.filesDir, "models")
            if (!dataDir.exists()) {
                dataDir.mkdirs()
            }

            return dataDir
        }
    }
}