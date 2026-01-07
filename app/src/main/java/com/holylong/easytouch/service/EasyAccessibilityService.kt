package com.holylong.easytouch.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class EasyAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: EasyAccessibilityService? = null
        fun getInstance(): EasyAccessibilityService? = instance
        fun isEnabled(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    fun performBackAction() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performRecentsAction() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    fun performHomeAction() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }
}
