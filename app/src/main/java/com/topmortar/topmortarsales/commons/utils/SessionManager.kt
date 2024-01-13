package com.topmortar.topmortarsales.commons.utils

import android.content.Context
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_ADMIN_CITY
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_BA
import com.topmortar.topmortarsales.commons.AUTH_LEVEL_COURIER
import com.topmortar.topmortarsales.commons.PRINT_METHOD_BLUETOOTH
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN
import com.topmortar.topmortarsales.commons.USER_KIND_ADMIN_CITY
import com.topmortar.topmortarsales.commons.USER_KIND_BA
import com.topmortar.topmortarsales.commons.USER_KIND_COURIER
import com.topmortar.topmortarsales.commons.USER_KIND_SALES
import com.topmortar.topmortarsales.model.UserModel

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

    fun setFullName(fullName: String) {
        editor.putString("fullName", fullName)
        editor.apply()
    }

    fun fullName(): String? {
        return sharedPreferences.getString("fullName", "")
    }

    fun setUserCityID(userCityID: String) {
        editor.putString("userCityID", userCityID)
        editor.apply()
    }

    fun userCityID(): String? {
        return sharedPreferences.getString("userCityID", "")
    }

    fun pinMapHint(pinMapHint: Boolean) {
        editor.putBoolean("pinMapHint", pinMapHint)
        editor.apply()
    }

    fun pinMapHint(): Boolean {
        return sharedPreferences.getBoolean("pinMapHint", false)
    }

    fun userBidLimit(userBidLimit: String) {
        editor.putString("userBidLimit", userBidLimit)
        editor.apply()
    }

    fun userBidLimit(): String? {
        return sharedPreferences.getString("userBidLimit", "0")
    }

    fun printState(printState: String) {
        editor.putString("printState", printState)
        editor.apply()
    }

    fun printState(): String? {
        return sharedPreferences.getString("printState", PRINT_METHOD_BLUETOOTH)
    }

    fun userDistributor(userDistributor: String) {
        editor.putString("userDistributor", userDistributor)
        editor.apply()
    }

    fun userDistributor(): String? {
        return sharedPreferences.getString("userDistributor", "0")
    }

    fun userDistributorNumber(userDistributorNumber: String) {
        editor.putString("userDistributorNumber", userDistributorNumber)
        editor.apply()
    }

    fun userDistributorNumber(): String? {
        return sharedPreferences.getString("userDistributorNumber", "0")
    }

    fun setUserLoggedIn(data: UserModel?) {
        if (data != null) {
            data.level_user = when (data.level_user) {
                AUTH_LEVEL_ADMIN -> USER_KIND_ADMIN
                AUTH_LEVEL_ADMIN_CITY -> USER_KIND_ADMIN_CITY
                AUTH_LEVEL_COURIER -> USER_KIND_COURIER
                AUTH_LEVEL_BA -> USER_KIND_BA
                else -> USER_KIND_SALES
            }
            setUserID(data.id_user)
            setUserKind(data.level_user)
            setUserName(data.username)
            setFullName(data.full_name)
            setUserCityID(data.id_city)
            userBidLimit(data.bid_limit)
            userDistributor(data.id_distributor)
            userDistributorNumber(data.nomorhp_distributor)
        } else {
            setUserID("")
            setUserKind("")
            setUserName("")
            setFullName("")
            setUserCityID("")
            userBidLimit("")
            userDistributor("")
            userDistributorNumber("")
        }
    }
}
