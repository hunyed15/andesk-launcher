package com.andesk.launcher.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.ui.home.HomeActivity

class KeyMappingAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "KeyMapping"
        var isRunning = false
    }

    private val handler = Handler(Looper.getMainLooper())
    private var lastDownTime = 0L
    private lateinit var prefs: PrefsManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        prefs = PrefsManager(this)
        Toast.makeText(this, "按键映射已启用", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false
        if (event.repeatCount > 0) return false
        if (event.keyCode != KeyEvent.KEYCODE_META_LEFT && event.keyCode != KeyEvent.KEYCODE_META_RIGHT) return false

        val now = event.downTime
        if (now - lastDownTime < 600) return true  // 防连按
        lastDownTime = now

        if (prefs.keyMappingSingleClick == "home") openAnDesk()
        return true
    }

    private fun openAnDesk() {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
        if (prefs.keyMappingShowToast) toast("返回安云桌面")
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}
