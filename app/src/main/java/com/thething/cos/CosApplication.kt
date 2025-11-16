package com.thething.cos

import android.app.Application
import com.thething.cos.lightledger.LightLedgerRuntimeSmoke
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LightLedgerRuntimeSmoke.verify(this)
    }
}
