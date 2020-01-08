package pl.sokolowskibartlomiej.naprawiamy

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class NaprawiamyApplication : Application() {

    init {
        instance = this@NaprawiamyApplication
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var instance: Context
    }
}