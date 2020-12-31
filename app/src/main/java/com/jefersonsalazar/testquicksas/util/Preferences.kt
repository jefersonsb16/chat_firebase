package com.jefersonsalazar.testquicksas.util

import android.content.SharedPreferences
import com.google.gson.Gson
import com.jefersonsalazar.testquicksas.model.User

class Preferences {

    var sharedPreferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null

    companion object {
        private var instance: Preferences? = null

        fun getInstance(): Preferences? {
            if (instance == null) {
                instance = Preferences()
            }
            return instance as Preferences
        }
    }

    fun setUserInfo(user: String) {
        editor!!.putString(Constants.TAG_USER_INFO, user).commit()
    }

    fun getUserInfo() : User {
        return Gson().fromJson(sharedPreferences?.getString(Constants.TAG_USER_INFO, ""), User::class.java)
    }
}