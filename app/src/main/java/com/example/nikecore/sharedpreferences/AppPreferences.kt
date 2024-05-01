package com.example.nikecore.sharedpreferences

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val ONBOARING_PREF = "onboardingprefs"
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("myprefs", 0)
    }

    fun getIsFirstStart(context: Context): Boolean {
        return getPrefs(context).getBoolean(ONBOARING_PREF, false)
    }


    fun setIsFirstStart(context: Context, value: Boolean?) {
        if (value != null) {
            getPrefs(context).edit().putBoolean(ONBOARING_PREF, value).apply()
        }
    }

    fun getInt(context: Context, key: String, defaultValue: Int) =
        getPrefs(context).getInt(key, defaultValue)

    fun setInt(context: Context, key: String, value: Int) {
        getPrefs(context).edit().putInt(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean) =
        getPrefs(context).getBoolean(key, defaultValue)

    fun setBoolean(context: Context, key: String, value: Boolean) {
        getPrefs(context).edit().putBoolean(key, value).apply()
    }

}