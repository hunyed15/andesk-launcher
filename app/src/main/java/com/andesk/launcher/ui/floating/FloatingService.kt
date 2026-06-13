package com.andesk.launcher.ui.floating

import android.app.AlertDialog
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.andesk.launcher.App
import com.andesk.launcher.R
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.ui.recent.RecentAppsActivity
import com.andesk.launcher.ui.settings.SettingsActivity
import com.andesk.launcher.util.MemoryUtils

class FloatingService : Service() {

    companion object {
        var isRunning = false
            private set
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var prefsManager: PrefsManager
    private lateinit var layoutParams: WindowManager.LayoutParams

    // 拖拽相关
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefsManager = PrefsManager(this)
        
        createFloatingView()
        showNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            windowManager.removeView(floatingView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createFloatingView() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_home, null)

        // 设置布局参数
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            
            // 恢复上次位置
            val savedX = prefsManager.floatingX
            val savedY = prefsManager.floatingY
            if (savedX >= 0 && savedY >= 0) {
                x = savedX
                y = savedY
            } else {
                // 默认位置：右侧中间
                val screenHeight = resources.displayMetrics.heightPixels
                x = 0
                y = screenHeight / 2
            }
        }

        // 设置透明度
        floatingView.alpha = prefsManager.floatingAlpha

        // 设置触摸事件
        setupTouchListener()

        // 添加到窗口
        windowManager.addView(floatingView, layoutParams)
    }

    private fun setupTouchListener() {
        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    
                    // 判断是否开始拖拽
                    if (!isDragging && (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10)) {
                        isDragging = true
                    }
                    
                    if (isDragging) {
                        layoutParams.x = initialX + deltaX.toInt()
                        layoutParams.y = initialY + deltaY.toInt()
                        windowManager.updateViewLayout(floatingView, layoutParams)
                    }
                    true
                }

                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // 点击事件 - 返回桌面
                        onFloatingClick()
                    } else {
                        // 拖拽结束，保存位置
                        prefsManager.floatingX = layoutParams.x
                        prefsManager.floatingY = layoutParams.y
                        
                        // 边缘吸附
                        snapToEdge()
                    }
                    true
                }

                else -> false
            }
        }

        // 长按显示菜单
        floatingView.setOnLongClickListener {
            showQuickMenu()
            true
        }
    }

    private fun onFloatingClick() {
        // 返回桌面
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun showQuickMenu() {
        val menuItems = arrayOf(
            "🏠 回到桌面",
            "📋 最近应用",
            "🧹 清理内存",
            "⚙️ 设置",
            "❌ 隐藏小圆点"
        )

        // 使用AlertDialog显示快捷菜单
        val builder = AlertDialog.Builder(this, R.style.FloatingDialogTheme)
        builder.setTitle("快捷操作")
        builder.setItems(menuItems) { _, which ->
            when (which) {
                0 -> onFloatingClick()
                1 -> openRecentApps()
                2 -> cleanMemory()
                3 -> openSettings()
                4 -> hideFloating()
            }
        }
        builder.setOnDismissListener {
            // 菜单关闭后恢复非焦点状态
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            windowManager.updateViewLayout(floatingView, layoutParams)
        }
        
        // 需要临时获取焦点来显示对话框
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        windowManager.updateViewLayout(floatingView, layoutParams)
        
        val dialog = builder.create()
        dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        dialog.show()
    }

    private fun openRecentApps() {
        val intent = Intent(this, RecentAppsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun cleanMemory() {
        val killed = MemoryUtils.killBackgroundProcesses(this)
        if (killed > 0) {
            Toast.makeText(this, "已清理 $killed 个后台应用", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "没有需要清理的后台应用", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    private fun hideFloating() {
        Toast.makeText(this, "小圆点已隐藏，可在设置中重新开启", Toast.LENGTH_SHORT).show()
        prefsManager.floatingEnabled = false
        stopSelf()
    }

    private fun snapToEdge() {
        val screenWidth = resources.displayMetrics.widthPixels
        val centerX = layoutParams.x + floatingView.width / 2
        
        // 吸附到最近的边缘
        layoutParams.x = if (centerX < screenWidth / 2) {
            0  // 左边缘
        } else {
            screenWidth - floatingView.width  // 右边缘
        }
        
        windowManager.updateViewLayout(floatingView, layoutParams)
        prefsManager.floatingX = layoutParams.x
    }

    private fun showNotification() {
        val intent = Intent(this, com.andesk.launcher.ui.home.HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(this, App.CHANNEL_FLOATING)
            .setContentTitle("AnDesk Launcher")
            .setContentText("Home小圆点运行中")
            .setSmallIcon(R.drawable.ic_home)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}
