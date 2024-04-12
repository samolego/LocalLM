package org.samo_lego.locallm.data

import android.content.SharedPreferences

private lateinit var settings: AppSettings
val appSettings: AppSettings
    get() = settings

class AppSettings(private val sharedPreferences: SharedPreferences) {
    fun setString(key: SettingsKeys, value: String) {
        sharedPreferences.edit().putString(key.toString(), value).apply()
    }

    fun getString(key: SettingsKeys, default: String): String {
        return sharedPreferences.getString(key.toString(), default) ?: default
    }

    fun setInt(key: SettingsKeys, value: Int) {
        sharedPreferences.edit().putInt(key.toString(), value).apply()
    }

    fun getInt(key: SettingsKeys, default: Int): Int {
        return sharedPreferences.getInt(key.toString(), default)
    }

    fun setBool(key: SettingsKeys, value: Boolean) {
        sharedPreferences.edit().putBoolean(key.toString(), value).apply()
    }

    fun getBool(key: SettingsKeys, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key.toString(), default)
    }

    fun setFloat(key: SettingsKeys, value: Float) {
        sharedPreferences.edit().putFloat(key.toString(), value).apply()
    }

    fun getFloat(key: SettingsKeys, default: Float): Float {
        return sharedPreferences.getFloat(key.toString(), default)
    }


    companion object {
        fun init(sharedPreferencesConstructor: (String) -> SharedPreferences) {
            settings = sharedPreferencesConstructor(settingsLabel).let(::AppSettings)
        }

        private const val settingsLabel = "Settings"
    }
}

enum class SettingsKeys(val key: String) {
    USE_TTS("useTTS"),
    WHISPER_LANGUAGE("whisperLanguage"),
    CLOUD_STT("cloudSTT"),
    AUTO_SEND("autoSendAfterRecognition"),
    LAST_MODEL("lastModel");

    override fun toString(): String {
        return key
    }
}
