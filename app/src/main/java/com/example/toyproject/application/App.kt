package com.example.toyproject.application

import android.app.Application

class App : Application() {

    companion object {
        lateinit  var instance : App
        private set
    }

    override fun onCreate() {
        instance = this
        super.onCreate()
    }
}