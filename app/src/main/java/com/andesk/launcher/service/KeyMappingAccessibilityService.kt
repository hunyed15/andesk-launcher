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
        private const val DOUBLE_CLICK_WINDOW_MS = 360L
        private const val LONG_PRESS_WINDOW_MS = 650L
        var isRunning = false
            private set

        private var instance: KeyMappingAccessibilityService? = null

        fun showPowerDialog(): Boolean {
            return instance?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) == true
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: PrefsManager
    private var winDownKeyCode = KeyEvent.KEYCODE_UNKNOWN
    private var winDownTime = 0L
    private var lastWinUpTime = 0L
    private var winComboDetected = false
    private var ignoreCurrentWinPress = false
    private var pendingWinOpen: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        instance = this
        prefs = PrefsManager(this)
        Toast.makeText(this, "按键映射已启用", Toast.LENGTH_SHORT).show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!::prefs.isInitialized) prefs = PrefsManager(this)
        if (!prefs.keyMappingEnabled || prefs.keyMappingSingleClick != "home") return false

        return handleWinKey(event)
    }

    private fun handleWinKey(event: KeyEvent): Boolean {
        val isWinKey = event.keyCode == KeyEvent.KEYCODE_META_LEFT ||
            event.keyCode == KeyEvent.KEYCODE_META_RIGHT

        if (!isWinKey) {
            if (winDownKeyCode != KeyEvent.KEYCODE_UNKNOWN && event.action == KeyEvent.ACTION_DOWN) {
                winComboDetected = true
            }
            return false
        }

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount > 0) {
                    winComboDetected = true
                    return false
                }

                if (pendingWinOpen != null && event.eventTime - lastWinUpTime < DOUBLE_CLICK_WINDOW_MS) {
                    cancelPendingWinOpen()
                    ignoreCurrentWinPress = true
                    winComboDetected = true
                } else {
                    ignoreCurrentWinPress = false
                    winComboDetected = false
                }

                winDownKeyCode = event.keyCode
                winDownTime = event.downTime
            }
            KeyEvent.ACTION_UP -> {
                if (winDownKeyCode != event.keyCode) return false

                val pressDuration = event.eventTime - winDownTime
                val shouldOpen = !ignoreCurrentWinPress &&
                    !winComboDetected &&
                    pressDuration < LONG_PRESS_WINDOW_MS

                winDownKeyCode = KeyEvent.KEYCODE_UNKNOWN
                winDownTime = 0L
                lastWinUpTime = event.eventTime

                if (shouldOpen) {
                    scheduleWinOpen()
                }
            }
        }

        return false
    }

    private fun scheduleWinOpen() {
        cancelPendingWinOpen()
        pendingWinOpen = Runnable {
            pendingWinOpen = null
            openAnDesk()
        }
        handler.postDelayed(pendingWinOpen!!, DOUBLE_CLICK_WINDOW_MS)
    }

    private fun cancelPendingWinOpen() {
        pendingWinOpen?.let { handler.removeCallbacks(it) }
        pendingWinOpen = null
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
        cancelPendingWinOpen()
        if (instance === this) instance = null
        isRunning = false
    }
}
