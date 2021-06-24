package me.darthwithap.airvedable

import android.annotation.SuppressLint
import android.content.Context
import android.app.Application

class BaseApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null
        fun getContext(): Context {
            return context!!
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

}