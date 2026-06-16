package com.andesk.launcher.ui.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.viewpager2.widget.ViewPager2
import com.andesk.launcher.R
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.data.model.AppInfo
import com.andesk.launcher.data.model.WeatherInfo
import com.andesk.launcher.data.repository.AppRepository
import com.andesk.launcher.data.repository.WeatherRepository
import com.andesk.launcher.receiver.PackageReceiver
import com.andesk.launcher.receiver.ScreenReceiver
import com.andesk.launcher.service.KeyMappingAccessibilityService
import com.andesk.launcher.ui.appdrawer.AppDrawerActivity
import com.andesk.launcher.ui.settings.SettingsActivity
import com.andesk.launcher.service.KeyMappingService
import com.andesk.launcher.util.MemoryUtils
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var appRepository: AppRepository
    private lateinit var weatherRepository: WeatherRepository
    
    // 分页
    private lateinit var viewPager: ViewPager2
    private lateinit var pageAdapter: AppPageAdapter
    private lateinit var pageIndicator: View
    
    // 时钟视图
    private lateinit var tvTime: TextView
    private lateinit var tvDate: TextView
    
    // 天气视图 (边栏)
    private lateinit var weatherIcon: ImageView
    private lateinit var weatherTemp: TextView
    private lateinit var weatherCity: TextView
    private lateinit var weatherDesc: TextView
    
    // 诗词视图 (边栏)
    private lateinit var tvHitokoto: TextView
    private lateinit var tvHitokotoFrom: TextView
    
    // Dock栏视图
    private lateinit var dockApp1: ImageView
    private lateinit var dockApp2: ImageView
    private lateinit var dockApp3: ImageView
    private lateinit var dockApp4: ImageView
    
    // 顶部视图
    private lateinit var statusTime: TextView
    private lateinit var tvBatteryPercent: TextView
    private lateinit var toastView: TextView
    
    // 广播接收器
    private val packageReceiver = PackageReceiver()
    private val screenReceiver = ScreenReceiver()
    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                PackageReceiver.ACTION_REFRESH_APPS -> {
                    appRepository.clearCache()
                    refreshApps()
                    loadDockApps()
                }
                ScreenReceiver.ACTION_SCREEN_ON -> {
                    loadWeather()
                    loadHitokoto()
                }
            }
        }
    }
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) {
                tvBatteryPercent.text = ((level * 100) / scale).toString()
            }
        }
    }
    private var isReceiverRegistered = false
    private var isScreenReceiverRegistered = false
    private var isRefreshReceiverRegistered = false
    private var isBatteryReceiverRegistered = false
    
    // 编辑模式
    private var isEditMode = false
    
    // Toast Handler
    private val handler = Handler(Looper.getMainLooper())
    
    // 一言刷新定时器
    private var hitokotoRefreshRunnable: Runnable? = null
    private val HITOKOTO_REFRESH_INTERVAL = 10 * 60 * 1000L  // 10分钟

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
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
        setupViewPager()
        setupDockBar()
        setupClickListeners()
        
        // 注册广播接收器
        registerPackageReceiver()
        
        // 启动按键映射服务
        startKeyMappingService()
        
        // 加载数据
        loadWeather()
        loadHitokoto()
        updateClock()
        
    }

    override fun onResume() {
        super.onResume()
        refreshApps()
        loadDockApps()
        loadWeather()
        loadHitokoto()
        startHitokotoRefreshTimer()
    }

    override fun onPause() {
        super.onPause()
        // 暂停定时刷新
        stopHitokotoRefreshTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterPackageReceiver()
        stopHitokotoRefreshTimer()
        handler.removeCallbacksAndMessages(null)
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
        viewPager = findViewById(R.id.viewPager)
        
        // 时钟
        tvTime = findViewById(R.id.tvTime)
        tvDate = findViewById(R.id.tvDate)
        
        // 天气 (边栏)
        weatherIcon = findViewById(R.id.weatherIcon)
        weatherTemp = findViewById(R.id.weatherTemp)
        weatherCity = findViewById(R.id.weatherCity)
        weatherDesc = findViewById(R.id.weatherDesc)
        
        // Dock栏
        dockApp1 = findViewById(R.id.dockIcon1)
        dockApp2 = findViewById(R.id.dockIcon2)
        dockApp3 = findViewById(R.id.dockIcon3)
        dockApp4 = findViewById(R.id.dockIcon4)
        
        // 诗词
        tvHitokoto = findViewById(R.id.tvHitokoto)
        tvHitokotoFrom = findViewById(R.id.tvHitokotoFrom)
        
        // 顶部
        statusTime = findViewById(R.id.statusTime)
        tvBatteryPercent = findViewById(R.id.tvBatteryPercent)
        toastView = findViewById(R.id.toastView)
    }

    // ==================== 分页设置 ====================

    private fun setupViewPager() {
        // 固定一行5个图标
        val columns = 5
        // 第一页2行，后续页面5行
        val firstPageRows = 2
        val otherPageRows = 5
        
        pageAdapter = AppPageAdapter(
            columnsPerRow = columns,
            firstPageRows = firstPageRows,
            otherPageRows = otherPageRows,
            onAppClick = { appInfo ->
                if (!isEditMode) {
                    appRepository.launchApp(appInfo.packageName)
                }
            },
            onAppLongClick = { appInfo ->
                enterEditMode()
                true
            },
            onFolderClick = { folder ->
                // 打开文件夹
                openFolder(folder)
            },
            onFolderLongClick = { folder ->
                // 长按文件夹
                enterEditMode()
                true
            },
            onUninstallClick = { appInfo ->
                appRepository.uninstallApp(appInfo.packageName)
            }
        )
        
        viewPager.apply {
            adapter = pageAdapter
            isUserInputEnabled = true
            // 平滑翻页动画
            setPageTransformer(com.andesk.launcher.util.SmoothPageTransformer())
        }
        
        // 设置页码指示器点击事件
        setupPageIndicator()
        
        // 页码指示器
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updatePageIndicator(position)
                setupDragForCurrentPage(position)
            }
        })
        viewPager.post { setupDragForCurrentPage(viewPager.currentItem) }
    }

    private fun setupPageIndicator() {
        val dots = listOf<TextView>(
            findViewById(R.id.dot1),
            findViewById(R.id.dot2),
            findViewById(R.id.dot3),
            findViewById(R.id.dot4)
        )
        
        dots.forEachIndexed { index, dot ->
            dot.setOnClickListener {
                viewPager.setCurrentItem(index, true)
            }
        }
    }

    private fun updatePageIndicator(currentPage: Int) {
        val dots = listOf<TextView>(
            findViewById(R.id.dot1),
            findViewById(R.id.dot2),
            findViewById(R.id.dot3),
            findViewById(R.id.dot4)
        )
        
        val totalPages = pageAdapter.getPageCount()
        
        dots.forEachIndexed { index, dot ->
            if (index < totalPages) {
                dot.visibility = View.VISIBLE
                if (index == currentPage) {
                    dot.setBackgroundResource(R.drawable.bg_page_indicator_active)
                    dot.setTextColor(getColor(R.color.on_primary))
                } else {
                    dot.setBackgroundResource(R.drawable.bg_page_indicator)
                    dot.setTextColor(getColor(R.color.muted))
                }
            } else {
                dot.visibility = View.GONE
            }
        }
    }

    // ==================== 权限检查 ====================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // 权限结果处理
                if (grantResults.isNotEmpty()) {
                    // 可以在这里处理权限结果
                }
            }
        }
    }

    // ==================== Dock栏 ====================

    private fun setupDockBar() {
        loadDockApps()
        
        // Dock栏应用点击事件
        dockApp1.setOnClickListener { launchDockApp(0) }
        dockApp2.setOnClickListener { launchDockApp(1) }
        dockApp3.setOnClickListener { launchDockApp(2) }
        dockApp4.setOnClickListener { launchDockApp(3) }
        
        // Dock栏应用长按事件
        dockApp1.setOnLongClickListener { showDockAppSelector(0); true }
        dockApp2.setOnLongClickListener { showDockAppSelector(1); true }
        dockApp3.setOnLongClickListener { showDockAppSelector(2); true }
        dockApp4.setOnLongClickListener { showDockAppSelector(3); true }
    }

    private fun loadDockApps() {
        val dockApps = appRepository.getDockApps()
        
        // 设置Dock应用图标，如果没有应用则显示+
        setDockIcon(dockApp1, dockApps.getOrNull(0))
        setDockIcon(dockApp2, dockApps.getOrNull(1))
        setDockIcon(dockApp3, dockApps.getOrNull(2))
        setDockIcon(dockApp4, dockApps.getOrNull(3))
    }

    private fun setDockIcon(imageView: ImageView, appInfo: AppInfo?) {
        if (appInfo != null) {
            imageView.clearColorFilter()
            imageView.setImageDrawable(appInfo.icon)
            imageView.alpha = 1.0f
        } else {
            imageView.setImageResource(R.drawable.ic_add)
            imageView.setColorFilter(ContextCompat.getColor(this, R.color.muted))
            imageView.alpha = 0.85f
        }
    }

    private fun launchDockApp(index: Int) {
        val dockApps = appRepository.getDockApps()
        if (index < dockApps.size) {
            appRepository.launchApp(dockApps[index].packageName)
        } else {
            showDockAppSelector(index)
        }
    }

    private fun showDockAppSelector(index: Int) {
        try {
            val allApps = appRepository.getInstalledApps()
            if (allApps.isEmpty()) {
                showToast("没有可用的应用")
                return
            }
            
            val appNames = allApps.map { it.name }.toTypedArray()
            
            android.app.AlertDialog.Builder(this)
                .setTitle("选择应用")
                .setItems(appNames) { _, which ->
                    if (which in allApps.indices) {
                        val selectedApp = allApps[which]
                        updateDockApp(index, selectedApp)
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("无法加载应用列表")
        }
    }

    private fun updateDockApp(index: Int, appInfo: AppInfo) {
        try {
            val currentDockApps = appRepository.getDockApps().toMutableList()
            
            while (currentDockApps.size <= index) {
                currentDockApps.add(appInfo)
            }
            
            currentDockApps[index] = appInfo
            appRepository.saveDockApps(currentDockApps)
            loadDockApps()
            showToast("已添加到Dock栏")
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("添加失败")
        }
    }

    // ==================== 主题切换 ====================

    private fun setupClickListeners() {
        // 所有应用按钮
        findViewById<View>(R.id.btnAllApps)?.setOnClickListener {
            startActivity(Intent(this, AppDrawerActivity::class.java))
        }
        
        // 搜索胶囊 → 打开应用抽屉（带搜索）
        findViewById<View>(R.id.searchCapsule)?.setOnClickListener {
            val intent = Intent(this, AppDrawerActivity::class.java)
            intent.putExtra("openSearch", true)
            startActivity(intent)
        }

        // WiFi图标 → 打开系统WiFi设置
        findViewById<View>(R.id.btnWifiSettings)?.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
        }

        // 电池图标 → 打开系统电池设置
        findViewById<View>(R.id.btnBatterySettings)?.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS))
        }
        
        // 设置按钮
        findViewById<View>(R.id.btnSettings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // 清理内存
        findViewById<View>(R.id.btnCleanup)?.setOnClickListener {
            cleanupMemory()
        }
        
        // 主题切换按钮
        findViewById<View>(R.id.btnThemeToggle)?.setOnClickListener {
            toggleTheme()
            val isDark = prefsManager.themeMode == "dark"
            findViewById<ImageView>(R.id.btnThemeToggle)?.setImageResource(
                if (isDark) R.drawable.ic_theme_dark else R.drawable.ic_theme_light
            )
        }

        // 电源菜单
        findViewById<View>(R.id.btnPowerMenu)?.setOnClickListener {
            if (!KeyMappingAccessibilityService.showPowerDialog()) {
                showToast("请开启无障碍服务后使用电源菜单")
            }
        }

        // 一言刷新按钮
        findViewById<View>(R.id.btnRefreshHitokoto)?.setOnClickListener {
            loadHitokoto()
        }
        
        // 点击空白区域退出编辑模式
        findViewById<View>(android.R.id.content).setOnClickListener {
            if (isEditMode) exitEditMode()
        }
    }

    private fun toggleTheme() {
        val currentMode = prefsManager.themeMode
        val newMode = if (currentMode == "dark") "light" else "dark"
        prefsManager.themeMode = newMode
        
        when (newMode) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        showToast(if (newMode == "dark") "已切换到深色模式" else "已切换到浅色模式")
    }

    // ==================== 编辑模式 ====================

    private fun enterEditMode() {
        if (isEditMode) return
        isEditMode = true
        pageAdapter.setEditMode(true)
        showToast("编辑模式 — 长按应用可创建文件夹")
    }

    private fun exitEditMode() {
        if (!isEditMode) return
        isEditMode = false
        pageAdapter.setEditMode(false)
    }

    // ==================== 时钟功能 ====================

    private fun updateClock() {
        val calendar = Calendar.getInstance()
        val is24Hour = prefsManager.is24HourFormat
        
        val timeFormat = if (is24Hour) "HH:mm" else "hh:mm"
        val timeStr = SimpleDateFormat(timeFormat, Locale.CHINA).format(calendar.time)
        tvTime.text = timeStr
        statusTime.text = timeStr
        
        val days = arrayOf("星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六")
        val dayOfWeek = days[calendar.get(Calendar.DAY_OF_WEEK) - 1]
        val dateStr = SimpleDateFormat("MM月dd日", Locale.CHINA).format(calendar.time) + " " + dayOfWeek.replace("星期", "周")
        tvDate.text = dateStr
        
        handler.postDelayed({ updateClock() }, 1000)
    }

    // ==================== 天气功能 ====================

    private fun loadWeather() {
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
        weatherCity.text = weather.city
        weatherDesc.text = weather.text
        weatherTemp.text = weather.tempRangeDisplay
        loadWeatherIcon(weather.icon)
        // 天气背景图标也加载
        loadWeatherBgIcon(weather.icon)
    }

    private fun loadWeatherBgIcon(iconCode: String) {
        val url = "https://a.hecdn.net/img/common/icon/202106d/$iconCode.png"
        val bgIcon = findViewById<ImageView>(R.id.weatherBgIcon)
        imageLoader.enqueue(ImageRequest.Builder(this).data(url).size(360)
            .target(bgIcon).build())
    }

    private fun showWeatherError() {
        weatherTemp.text = "--°~--°"
        weatherCity.text = prefsManager.weatherCity
        weatherDesc.text = "暂无数据"
    }

    private fun loadWeatherIcon(iconCode: String) {
        val localRes = when (iconCode.toIntOrNull() ?: 100) {
            in 100..103 -> R.drawable.qweather_100
            in 104..299 -> R.drawable.qweather_104
            in 300..399 -> R.drawable.qweather_305
            in 400..499 -> R.drawable.qweather_400
            else -> R.drawable.qweather_104
        }
        val url = "https://a.hecdn.net/img/common/icon/202106d/$iconCode.png"
        imageLoader.enqueue(ImageRequest.Builder(this).data(url).size(136)
            .placeholder(localRes).error(localRes).target(weatherIcon).build())
    }

    // ==================== 今日诗词功能 ====================

    private fun loadHitokoto() {
        lifecycleScope.launch {
            try {
                val client = com.jinrishici.sdk.android.JinrishiciClient.getInstance()
                val sentence = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.getOneSentence()
                }
                if (sentence != null) {
                    // 结构: sentence.data.content, sentence.data.origin.author/title/dynasty
                    val data = sentence.javaClass.getDeclaredField("data").apply { isAccessible = true }.get(sentence)
                    val content = data?.javaClass?.getDeclaredField("content")?.apply { isAccessible = true }?.get(data) as? String ?: ""
                    val origin = data?.javaClass?.getDeclaredField("origin")?.apply { isAccessible = true }?.get(data)
                    val author = origin?.javaClass?.getDeclaredField("author")?.apply { isAccessible = true }?.get(origin) as? String ?: ""
                    val title = origin?.javaClass?.getDeclaredField("title")?.apply { isAccessible = true }?.get(origin) as? String ?: ""
                    val dynasty = origin?.javaClass?.getDeclaredField("dynasty")?.apply { isAccessible = true }?.get(origin) as? String ?: ""
                    
                    tvHitokoto.text = content
                    tvHitokotoFrom.text = when {
                        title.isNotEmpty() && dynasty.isNotEmpty() -> " ——$dynasty·$author《$title》"
                        title.isNotEmpty() -> " ——$author《$title》"
                        author.isNotEmpty() -> " ——$author"
                        else -> ""
                    }
                } else {
                    showHitokotoError()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showHitokotoError()
            }
        }
    }

    private fun startHitokotoRefreshTimer() {
        stopHitokotoRefreshTimer()
        hitokotoRefreshRunnable = object : Runnable {
            override fun run() {
                loadHitokoto()
                handler.postDelayed(this, HITOKOTO_REFRESH_INTERVAL)
            }
        }
        handler.postDelayed(hitokotoRefreshRunnable!!, HITOKOTO_REFRESH_INTERVAL)
    }

    private fun stopHitokotoRefreshTimer() {
        hitokotoRefreshRunnable?.let {
            handler.removeCallbacks(it)
        }
        hitokotoRefreshRunnable = null
    }

    private fun showHitokotoError() {
        tvHitokoto.text = "人生如戏，唯有入戏，方能始终。"
        tvHitokotoFrom.text = " ——慕夜"
    }

    // ==================== 应用管理 ====================

    private fun refreshApps() {
        val apps = appRepository.getSortedApps()
        val folders = appRepository.getAllFolders()
        pageAdapter.setApps(apps, folders)
        updatePageIndicator(0)
    }

    private fun openFolder(folder: com.andesk.launcher.data.model.Folder) {
        val allApps = appRepository.getInstalledApps()
        val dialog = FolderDialog(
            folder = folder,
            allApps = allApps,
            appRepository = appRepository,
            onFolderUpdated = {
                refreshApps()
            }
        )
        dialog.show(supportFragmentManager, "folder_dialog")
    }

    private fun setupDragForCurrentPage(position: Int) {
        val currentPage = viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView ?: return
        val viewHolder = currentPage.findViewHolderForAdapterPosition(position) as? AppPageAdapter.PageViewHolder ?: return
        val recyclerView = viewHolder.recyclerView
        val adapter = viewHolder.getAdapter() ?: return

        val dragHelper = DragHelper(
            adapter = adapter,
            viewPager = viewPager,
            onDragEnd = { fromPos, toPos ->
                // 保存新的排序
                pageAdapter.updatePageItems(position, adapter.getItems())
                appRepository.saveAppOrder(pageAdapter.getAppOrder())
            },
            onMergeToFolder = { fromPos, toPos ->
                // 合并到文件夹
                val items = adapter.getItems()
                val fromItem = items[fromPos]
                val toItem = items[toPos]

                if (fromItem is DesktopItem.App && toItem is DesktopItem.App) {
                    // 两个应用合并为文件夹
                    val folder = appRepository.mergeToFolder(
                        fromItem.appInfo.packageName,
                        toItem.appInfo.packageName
                    )
                    refreshApps()
                    showToast("已创建文件夹: ${folder.name}")
                } else if (fromItem is DesktopItem.App && toItem is DesktopItem.FolderItem) {
                    // 应用添加到文件夹
                    appRepository.addAppToFolder(toItem.folder.id, fromItem.appInfo.packageName)
                    refreshApps()
                    showToast("已添加到文件夹")
                }
            }
        )

        val itemTouchHelper = ItemTouchHelper(dragHelper)
        itemTouchHelper.attachToRecyclerView(recyclerView)
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
            registerReceiver(packageReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
            isReceiverRegistered = true
        }
        // 注册屏幕解锁接收器
        if (!isScreenReceiverRegistered) {
            val screenFilter = IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            registerReceiver(screenReceiver, screenFilter, ContextCompat.RECEIVER_EXPORTED)
            isScreenReceiverRegistered = true
        }
        if (!isRefreshReceiverRegistered) {
            val refreshFilter = IntentFilter().apply {
                addAction(PackageReceiver.ACTION_REFRESH_APPS)
                addAction(ScreenReceiver.ACTION_SCREEN_ON)
            }
            registerReceiver(refreshReceiver, refreshFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
            isRefreshReceiverRegistered = true
        }
        if (!isBatteryReceiverRegistered) {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            isBatteryReceiverRegistered = true
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
        if (isScreenReceiverRegistered) {
            try {
                unregisterReceiver(screenReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isScreenReceiverRegistered = false
        }
        if (isRefreshReceiverRegistered) {
            try {
                unregisterReceiver(refreshReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isRefreshReceiverRegistered = false
        }
        if (isBatteryReceiverRegistered) {
            try {
                unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isBatteryReceiverRegistered = false
        }
    }

    // ==================== 清理内存 ====================

    private fun cleanupMemory() {
        val mem = MemoryUtils.getMemoryInfo(this)
        System.gc()
        Runtime.getRuntime().gc()
        val used = String.format("%.1fG", mem.usedMB / 1024.0)
        val total = String.format("%.1fG", mem.totalMB / 1024.0)
        showToast("🚀 内存: $used/$total (${mem.usagePercent}%)")
    }

    // ==================== 按键映射服务 ====================

    private fun startKeyMappingService() {
        if (prefsManager.keyMappingEnabled && !KeyMappingService.isRunning) {
            try {
                val intent = Intent(this, KeyMappingService::class.java)
                startForegroundService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==================== 性能优化 ====================

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        when (level) {
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                System.gc()
            }
        }
    }

    // ==================== Toast提示 ====================

    private fun showToast(message: String) {
        toastView.text = message
        toastView.visibility = View.VISIBLE
        
        val fadeIn = ObjectAnimator.ofFloat(toastView, "alpha", 0f, 1f)
        fadeIn.duration = 200
        fadeIn.start()
        
        handler.postDelayed({
            val fadeOut = ObjectAnimator.ofFloat(toastView, "alpha", 1f, 0f)
            fadeOut.duration = 300
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    toastView.visibility = View.GONE
                }
            })
            fadeOut.start()
        }, 2000)
    }
}
