package com.andesk.launcher.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.ui.home.HomeActivity
import com.andesk.launcher.util.KeyMappingKeys

class KeyMappingAccessibilityService : AccessibilityService() {

    companion object {
        private const val DOUBLE_CLICK_WINDOW_MS = 360L
        private const val LONG_PRESS_WINDOW_MS = 650L

        /** 自定义按键录制完成后发出的广播（携带键码 keyCode） */
        const val ACTION_KEY_CAPTURED = "com.andesk.launcher.action.KEY_CAPTURED"

        var isRunning = false
            private set

        /** 是否处于"自定义按键录制"等待状态（内存态，设置页控制） */
        @Volatile
        var capturePending = false

        private var instance: KeyMappingAccessibilityService? = null

        fun showPowerDialog(): Boolean {
            return instance?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG) == true
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: PrefsManager

    // 触发键状态（原为 Win 键，现为可配置触发键）
    private var triggerDownKeyCode = KeyEvent.KEYCODE_UNKNOWN
    private var triggerDownTime = 0L
    private var lastTriggerUpTime = 0L
    private var comboDetected = false
    private var ignoreCurrentTriggerPress = false
    private var pendingTriggerOpen: Runnable? = null

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

        // 自定义按键录制优先
        if (capturePending) {
            return handleCapture(event)
        }

        if (!prefs.keyMappingEnabled || prefs.keyMappingSingleClick != "home") return false

        return handleTriggerKey(event)
    }

    /**
     * 录制模式：捕获下一个非修饰键作为触发键。Esc 取消。
     * 在按下（DOWN）时捕获并消费事件，避免默认行为（如 Back 返回）干扰。
     */
    private fun handleCapture(event: KeyEvent): Boolean {
        // 修饰键不采集（Win/Shift/Ctrl/Alt 等）
        if (KeyMappingKeys.isModifier(event.keyCode)) return false

        // Esc 取消录制
        if (event.keyCode == KeyEvent.KEYCODE_ESCAPE) {
            if (event.action == KeyEvent.ACTION_UP) {
                capturePending = false
                toast("已取消自定义按键")
            }
            return false
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            capturePending = false
            prefs.keyMappingKeyCode = event.keyCode
            toast("已设置按键: " + KeyMappingKeys.labelFor(event.keyCode))
            // 通知设置页刷新（自定义按键可能不在预置列表）
            sendBroadcast(Intent(ACTION_KEY_CAPTURED).setPackage(packageName).putExtra("keyCode", event.keyCode))
            return true
        }

        return false
    }

    private fun handleTriggerKey(event: KeyEvent): Boolean {
        val isTrigger = KeyMappingKeys.matches(prefs.keyMappingKeyCode, event.keyCode)

        if (!isTrigger) {
            if (triggerDownKeyCode != KeyEvent.KEYCODE_UNKNOWN && event.action == KeyEvent.ACTION_DOWN) {
                comboDetected = true
            }
            return false
        }

        when (event.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount > 0) {
                    comboDetected = true
                    return false
                }

                if (pendingTriggerOpen != null && event.eventTime - lastTriggerUpTime < DOUBLE_CLICK_WINDOW_MS) {
                    cancelPendingTriggerOpen()
                    ignoreCurrentTriggerPress = true
                    comboDetected = true
                } else {
                    ignoreCurrentTriggerPress = false
                    comboDetected = false
                }

                triggerDownKeyCode = event.keyCode
                triggerDownTime = event.downTime
            }
            KeyEvent.ACTION_UP -> {
                if (triggerDownKeyCode != event.keyCode) return false

                val pressDuration = event.eventTime - triggerDownTime
                val shouldOpen = !ignoreCurrentTriggerPress &&
                    !comboDetected &&
                    pressDuration < LONG_PRESS_WINDOW_MS

                triggerDownKeyCode = KeyEvent.KEYCODE_UNKNOWN
                triggerDownTime = 0L
                lastTriggerUpTime = event.eventTime

                if (shouldOpen) {
                    scheduleTriggerOpen()
                }
            }
        }

        return false
    }

    private fun scheduleTriggerOpen() {
        cancelPendingTriggerOpen()
        pendingTriggerOpen = Runnable {
            pendingTriggerOpen = null
            openAnDesk()
        }
        handler.postDelayed(pendingTriggerOpen!!, DOUBLE_CLICK_WINDOW_MS)
    }

    private fun cancelPendingTriggerOpen() {
        pendingTriggerOpen?.let { handler.removeCallbacks(it) }
        pendingTriggerOpen = null
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
        cancelPendingTriggerOpen()
        if (instance === this) instance = null
        isRunning = false
    }
}
