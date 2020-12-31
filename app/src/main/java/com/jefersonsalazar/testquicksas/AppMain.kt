package com.jefersonsalazar.testquicksas

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class AppMain : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}