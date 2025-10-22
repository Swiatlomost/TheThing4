package com.example.thething4.overlay

import android.Manifest
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LifecycleOverlayServiceTest {

    @Before
    fun grantPermissions() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.adoptShellPermissionIdentity(
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.FOREGROUND_SERVICE,
            "android.permission.FOREGROUND_SERVICE_SPECIAL_USE"
        )
    }

    @After
    fun revokePermissions() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.dropShellPermissionIdentity()
    }

    @Test
    fun startAndStopService_doesNotCrash() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val intent = Intent(context, LifecycleOverlayService::class.java)
        context.startService(intent)
        instrumentation.waitForIdleSync()
        context.stopService(intent)
        instrumentation.waitForIdleSync()
    }
}
