package com.example.aiartgenerator.utils

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.TextUtils
import androidx.annotation.Keep
import androidx.core.content.edit

class AppPrefs(appContext: Context?) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext)

    fun getInt(key: String?): Int {
        return preferences.getInt(key, 0)
    }


    fun getListInt(key: String?): ArrayList<Int> {
        val myList = TextUtils.split(preferences.getString(key, ""), "‚‗‚")
        val arrayToList = ArrayList(listOf(*myList))
        val newList = ArrayList<Int>()
        for (item in arrayToList) newList.add(item.toInt())
        return newList
    }

    fun getString(key: String?): String? {
        return preferences.getString(key, "")
    }


    fun getBoolean(key: String?): Boolean {
        return preferences.getBoolean(key, true)
    }

    fun putInt(key: String?, value: Int) {
        checkForNullKey(key)
        preferences.edit().putInt(key, value).apply()
    }

    fun putLong(key: String?, value: Long) {
        checkForNullKey(key)
        preferences.edit().putLong(key, value).apply()
    }

    fun getLong(key: String?): Long {
        return preferences.getLong(key, 0L)
    }


    fun putBoolean(key: String?, value: Boolean) {
        checkForNullKey(key)
        preferences.edit().putBoolean(key, value).apply()
    }

    fun remove(key: String?) {
        preferences.edit().remove(key).apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun checkForNullKey(key: String?) {
        if (key == null) {
            throw NullPointerException()
        }
    }

    fun putStringLang(keyLanguageApp: String, code: String) {
        checkForNullKey(keyLanguageApp)
        preferences.edit().putString(keyLanguageApp, code).apply()
    }
    
    fun putString(keyString: String, code: String) {
        checkForNullKey(keyString)
        preferences.edit().putString(keyString, code).apply()
    }
    
   

    fun getStringLang(keyLanguageApp: String?): String? {
        return preferences.getString(keyLanguageApp, "es")
    }


    @Keep
    data class SelectedIconAndBackground(val selectedIcon: Int, val selectedBackground: Int)

    fun saveSelectedIconAndBackground(selectedValues: SelectedIconAndBackground) {
        preferences.edit {
            putInt("KEY_SELECTED_ICON", selectedValues.selectedIcon)
            putInt("KEY_SELECTED_BACKGROUND", selectedValues.selectedBackground)
        }
    }

    fun getSelectedIconAndBackground(): SelectedIconAndBackground {
        return SelectedIconAndBackground(
            preferences.getInt("KEY_SELECTED_ICON", 0),
            preferences.getInt("KEY_SELECTED_BACKGROUND", 0)
        )
    }


}