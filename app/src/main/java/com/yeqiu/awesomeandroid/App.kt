package com.yeqiu.awesomeandroid

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.yeqiu.common.log

class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this@App)
    }

    override fun onCreate() {
        super.onCreate()
        log("App Start !!!")

    }

}