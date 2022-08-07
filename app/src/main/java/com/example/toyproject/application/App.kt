package com.example.toyproject.application

import android.app.Application

class App : Application() {

    companion object {
        var instance : App? = null
        private set
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }
}