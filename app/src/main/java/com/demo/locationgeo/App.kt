package com.demo.locationgeo

import android.app.Application
import android.content.Context

class App : Application() {

    companion object {
        private const val TAG = "GEO_App"

        private var mContext: Context? = null

        internal fun getContext() = mContext
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
    }
}