package com.andesk.launcher.ui.home

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andesk.launcher.R
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.data.model.AppInfo
import com.andesk.launcher.data.model.WeatherInfo
import com.andesk.launcher.data.repository.AppRepository
import com.andesk.launcher.data.repository.WeatherRepository
import com.andesk.launcher.receiver.PackageReceiver
import com.andesk.launcher.ui.appdrawer.AppDrawerActivity
import com.andesk.launcher.ui.floating.FloatingService
import com.andesk.launcher.ui.settings.SettingsActivity
import com.andesk.launcher.util.WeatherUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var appRepository: AppRepository
    private lateinit var weatherRepository: WeatherRepository
    
    private lateinit var appGridAdapter: AppGridAdapter
    private lateinit var recyclerView: RecyclerView
    
    // 时钟视图
    private lateinit var tvTime: TextView
    private lateinit var tvDate: TextView
    
    // 天气视图
    private lateinit var cardWeather: CardView
    private lateinit var ivWeatherIcon: ImageView
    private lateinit var tvWeatherTemp: TextView
    private lateinit var tvWeatherText: TextView
    private lateinit var tvWeatherCity: TextView
    
    // Dock栏视图
    private lateinit var dockApp1: ImageView
    private lateinit var dockApp2: ImageView
    private lateinit var dockApp3: ImageView
    private lateinit var dockApp4: ImageView
    
    // 广播接收器
    private val packageReceiver = PackageReceiver()
    private var isReceiverRegistered = false

    companion object {
        private const val LOCATION_PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 全屏模式
        setupFullScreen()
        
        setContentView(R.layout.activity_home)
        
        // 初始化
        prefsManager = PrefsManager(this)
        appRepository = AppRepository(this, prefsManager)
        weatherRepository = WeatherRepository(prefsManager)
        
        // 初始化UI
        initViews()
        setupAppGrid()
        setupDockBar()
        setupClickListeners()
        
        // 注册广播接收器
        registerPackageReceiver()
        
        // 启动服务
        startFloatingService()
        
        // 加载数据
        loadWeather()
        updateClock()
        
        // 检查权限
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        refreshApps()
        loadDockApps()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterPackageReceiver()
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
        // 应用网格
        recyclerView = findViewById(R.id.rvApps)
        
        // 时钟
        tvTime = findViewById(R.id.tvTime)
        tvDate = findViewById(R.id.tvDate)
        
        // 天气卡片
        cardWeather = findViewById(R.id.cardWeather)
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon)
        tvWeatherTemp = findViewById(R.id.tvWeatherTemp)
        tvWeatherText = findViewById(R.id.tvWeatherText)
        tvWeatherCity = findViewById(R.id.tvWeatherCity)
        
        // Dock栏
        dockApp1 = findViewById(R.id.dockApp1)
        dockApp2 = findViewById(R.id.dockApp2)
        dockApp3 = findViewById(R.id.dockApp3)
        dockApp4 = findViewById(R.id.dockApp4)
        
        // 设置天气卡片可见性
        cardWeather.visibility = if (prefsManager.showWeather) View.VISIBLE else View.GONE
    }

    private fun setupAppGrid() {
        appGridAdapter = AppGridAdapter(
            onAppClick = { appInfo ->
                appRepository.launchApp(appInfo.packageName)
            },
            onAppLongClick = { appInfo ->
                showAppOptions(appInfo)
                true
            }
        )
        
        // 根据屏幕尺寸决定列数
        val spanCount = when {
            resources.configuration.smallestScreenWidthDp >= 800 -> 6  // 12寸+
            resources.configuration.smallestScreenWidthDp >= 600 -> 5  // 10寸+
            else -> 4  // 手机
        }
        
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@HomeActivity, spanCount)
            adapter = appGridAdapter
        }
    }

    private fun setupDockBar() {
        loadDockApps()
        
        // Dock栏应用点击事件
        dockApp1.setOnClickListener { launchDockApp(0) }
        dockApp2.setOnClickListener { launchDockApp(1) }
        dockApp3.setOnClickListener { launchDockApp(2) }
        dockApp4.setOnClickListener { launchDockApp(3) }
        
        // Dock栏应用长按事件（更换应用）
        dockApp1.setOnLongClickListener { showDockAppSelector(0); true }
        dockApp2.setOnLongClickListener { showDockAppSelector(1); true }
        dockApp3.setOnLongClickListener { showDockAppSelector(2); true }
        dockApp4.setOnLongClickListener { showDockAppSelector(3); true }
    }

    private fun setupClickListeners() {
        // 所有应用按钮
        findViewById<View>(R.id.btnAllApps)?.setOnClickListener {
            startActivity(Intent(this, AppDrawerActivity::class.java))
        }
        
        // 设置按钮
        findViewById<View>(R.id.btnSettings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    // ==================== 时钟功能 ====================

    private fun updateClock() {
        val calendar = Calendar.getInstance()
        val is24Hour = prefsManager.is24HourFormat
        
        val timeFormat = if (is24Hour) "HH:mm" else "hh:mm a"
        val dateFormat = "yyyy年MM月dd日 EEEE"
        
        val timeStr = SimpleDateFormat(timeFormat, Locale.CHINA).format(calendar.time)
        val dateStr = SimpleDateFormat(dateFormat, Locale.CHINA).format(calendar.time)
        
        tvTime.text = timeStr
        tvDate.text = dateStr
        
        // 每秒更新
        window.decorView.postDelayed({ updateClock() }, 1000)
    }

    // ==================== 天气功能 ====================

    private fun loadWeather() {
        if (!prefsManager.showWeather) {
            cardWeather.visibility = View.GONE
            return
        }
        
        cardWeather.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val weather = weatherRepository.getWeather()
                if (weather != null) {
                    updateWeatherUI(weather)
                } else {
                    showWeatherError()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showWeatherError()
            }
        }
    }

    private fun updateWeatherUI(weather: WeatherInfo) {
        tvWeatherTemp.text = weather.tempDisplay
        tvWeatherText.text = weather.text
        tvWeatherCity.text = weather.city
        
        // 设置天气图标
        val iconResName = weather.iconResName
        val iconResId = resources.getIdentifier(iconResName, "drawable", packageName)
        if (iconResId != 0) {
            ivWeatherIcon.setImageResource(iconResId)
        } else {
            // 默认图标
            ivWeatherIcon.setImageResource(R.drawable.ic_weather_sunny)
        }
    }

    private fun showWeatherError() {
        tvWeatherTemp.text = "--°C"
        tvWeatherText.text = "天气不可用"
        tvWeatherCity.text = prefsManager.weatherCity
    }

    // ==================== Dock栏功能 ====================

    private fun loadDockApps() {
        val dockApps = appRepository.getDockApps()
        
        // 清空所有Dock图标
        dockApp1.setImageDrawable(null)
        dockApp2.setImageDrawable(null)
        dockApp3.setImageDrawable(null)
        dockApp4.setImageDrawable(null)
        
        // 设置Dock应用图标
        if (dockApps.isNotEmpty()) dockApp1.setImageDrawable(dockApps[0].icon)
        if (dockApps.size > 1) dockApp2.setImageDrawable(dockApps[1].icon)
        if (dockApps.size > 2) dockApp3.setImageDrawable(dockApps[2].icon)
        if (dockApps.size > 3) dockApp4.setImageDrawable(dockApps[3].icon)
    }

    private fun launchDockApp(index: Int) {
        val dockApps = appRepository.getDockApps()
        if (index < dockApps.size) {
            appRepository.launchApp(dockApps[index].packageName)
        }
    }

    private fun showDockAppSelector(index: Int) {
        val allApps = appRepository.getInstalledApps()
        val appNames = allApps.map { it.name }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle("选择应用")
            .setItems(appNames) { _, which ->
                val selectedApp = allApps[which]
                updateDockApp(index, selectedApp)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun updateDockApp(index: Int, appInfo: AppInfo) {
        val currentDockApps = appRepository.getDockApps().toMutableList()
        
        // 确保列表足够长
        while (currentDockApps.size <= index) {
            currentDockApps.add(appInfo)
        }
        
        currentDockApps[index] = appInfo
        appRepository.saveDockApps(currentDockApps)
        loadDockApps()
    }

    // ==================== 应用管理 ====================

    private fun refreshApps() {
        val apps = appRepository.getInstalledApps(forceRefresh = true)
        appGridAdapter.submitList(apps)
    }

    private fun showAppOptions(appInfo: AppInfo) {
        val options = arrayOf("打开", "卸载", "应用信息", "添加到Dock")
        
        AlertDialog.Builder(this)
            .setTitle(appInfo.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> appRepository.launchApp(appInfo.packageName)
                    1 -> appRepository.uninstallApp(appInfo.packageName)
                    2 -> com.andesk.launcher.util.AppUtils.openAppSettings(this, appInfo.packageName)
                    3 -> addToDock(appInfo)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun addToDock(appInfo: AppInfo) {
        val currentDockApps = appRepository.getDockApps().toMutableList()
        
        // 检查是否已存在
        if (currentDockApps.any { it.packageName == appInfo.packageName }) {
            Toast.makeText(this, "该应用已在Dock栏", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 添加到Dock（最多5个）
        if (currentDockApps.size < 5) {
            currentDockApps.add(appInfo)
            appRepository.saveDockApps(currentDockApps)
            loadDockApps()
            Toast.makeText(this, "已添加到Dock栏", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Dock栏已满", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== 广播接收器 ====================

    private fun registerPackageReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            registerReceiver(packageReceiver, filter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterPackageReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(packageReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }
    }

    // ==================== 服务启动 ====================

    private fun startFloatingService() {
        if (prefsManager.floatingEnabled) {
            if (FloatingService.isRunning) return
            
            val intent = Intent(this, FloatingService::class.java)
            startForegroundService(intent)
        }
    }

    // ==================== 权限检查 ====================

    private fun checkPermissions() {
        val permissions = mutableListOf<String>()
        
        // 位置权限（用于天气定位）
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        // 悬浮窗权限
        if (!android.provider.Settings.canDrawOverlays(this)) {
            // 引导用户开启悬浮窗权限
            showOverlayPermissionDialog()
        }
        
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要悬浮窗权限")
            .setMessage("Home小圆点功能需要悬浮窗权限，请在设置中开启")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(
                    android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
            .setNegativeButton("暂不开启", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadWeather()
                }
            }
        }
    }
}
