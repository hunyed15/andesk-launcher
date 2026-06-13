package com.andesk.launcher.ui.settings

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.andesk.launcher.R
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.util.DeviceUtils

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        setContentView(R.layout.activity_settings)

        prefsManager = PrefsManager(this)
        
        initViews()
        loadSettings()
    }

    private fun setupFullScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }

    private fun initViews() {
        // 返回按钮
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // 24小时制
        findViewById<Switch>(R.id.switch24Hour)?.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.is24HourFormat = isChecked
        }

        // 显示天气
        findViewById<Switch>(R.id.switchWeather)?.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.showWeather = isChecked
        }

        // Home小圆点
        findViewById<Switch>(R.id.switchFloating)?.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.floatingEnabled = isChecked
            // TODO: 启动/停止FloatingService
        }

        // 设备信息
        val tvDeviceInfo = findViewById<TextView>(R.id.tvDeviceInfo)
        tvDeviceInfo?.text = buildString {
            append("设备: ${DeviceUtils.getDeviceModel()}\n")
            append("系统: ${DeviceUtils.getAndroidVersion()}\n")
            append("内存: ${DeviceUtils.getTotalMemoryMB(this@SettingsActivity)}MB")
            if (DeviceUtils.isLowMemoryDevice(this@SettingsActivity)) {
                append(" (低内存设备)")
            }
        }

        // 版本号
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            findViewById<TextView>(R.id.tvVersion)?.text = "版本: ${packageInfo.versionName}"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSettings() {
        findViewById<Switch>(R.id.switch24Hour)?.isChecked = prefsManager.is24HourFormat
        findViewById<Switch>(R.id.switchWeather)?.isChecked = prefsManager.showWeather
        findViewById<Switch>(R.id.switchFloating)?.isChecked = prefsManager.floatingEnabled
    }
}
