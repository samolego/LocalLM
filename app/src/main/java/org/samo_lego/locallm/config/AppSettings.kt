package org.samo_lego.locallm.config

import android.content.SharedPreferences

private lateinit var settings: AppSettings
val appSettings: AppSettings
    get() = settings

class AppSettings(private val sharedPreferences: SharedPreferences) {
    fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String, default: String): String {
        return sharedPreferences.getString(key, default) ?: default
    }

    fun setInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    fun setBool(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBool(key: String, default: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun setFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    fun getFloat(key: String, default: Float): Float {
        return sharedPreferences.getFloat(key, default)
    }


    companion object {
        fun init(sharedPreferencesConstructor: (String) -> SharedPreferences) {
            settings = sharedPreferencesConstructor(settingsLabel).let(::AppSettings)
        }

        private const val settingsLabel = "Settings"
    }
}
