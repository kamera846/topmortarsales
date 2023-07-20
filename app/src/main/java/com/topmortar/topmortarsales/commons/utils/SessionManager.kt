package com.topmortar.topmortarsales.commons.utils

import android.content.Context

class SessionManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("Session", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun setLoggedIn(isLoggedIn: Boolean) {
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun setUserKind(userKind: String) {
        editor.putString("userKind", userKind)
        editor.apply()
    }

    fun userKind(): String? {
        return sharedPreferences.getString("userKind", "")
    }

    fun setUserID(userID: String) {
        editor.putString("userID", userID)
        editor.apply()
    }

    fun userID(): String? {
        return sharedPreferences.getString("userID", "")
    }

    fun setUserName(userName: String) {
        editor.putString("userName", userName)
        editor.apply()
    }

    fun userName(): String? {
        return sharedPreferences.getString("userName", "")
    }

    fun setUserCityID(userCityID: String) {
        editor.putString("userCityID", userCityID)
        editor.apply()
    }

    fun userCityID(): String? {
        return sharedPreferences.getString("userCityID", "")
    }
}
