package com.andesk.launcher.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.andesk.launcher.R
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.service.KeyMappingService
import com.andesk.launcher.util.DeviceUtils
import kotlinx.coroutines.launch

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

        // 城市选择（省→市级联）
        CityData.setupSpinners(this,
            findViewById(R.id.spinnerProvince),
            findViewById(R.id.spinnerCity),
            prefsManager.weatherCity
        ) { name, adcode ->
            prefsManager.weatherCity = name
            prefsManager.weatherCityId = adcode
            prefsManager.clearWeatherCache()
        }

        // 定位按钮
        findViewById<View>(R.id.btnLocation)?.setOnClickListener { requestLocation() }

        // ========== 按键映射设置 ==========
        
        // 按键映射开关
        findViewById<Switch>(R.id.switchKeyMapping)?.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.keyMappingEnabled = isChecked
            updateKeyMappingVisibility(isChecked)
            if (isChecked) {
                startKeyMappingService()
                Toast.makeText(this, "按键映射已开启", Toast.LENGTH_SHORT).show()
            } else {
                stopKeyMappingService()
                Toast.makeText(this, "按键映射已关闭", Toast.LENGTH_SHORT).show()
            }
        }

        // 单击动作选择
        setupSingleClickSpinner()
        
        // 显示提示
        findViewById<Switch>(R.id.switchShowToast)?.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.keyMappingShowToast = isChecked
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

        // 无障碍服务按钮
        findViewById<View>(R.id.btnAccessibility)?.setOnClickListener {
            openAccessibilitySettings()
        }

        // 设置为默认桌面
        findViewById<View>(R.id.btnSetDefaultHome)?.setOnClickListener {
            val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Toast.makeText(this, "请选择「安云桌面」→「始终」", Toast.LENGTH_LONG).show()
        }

        // 开机自启开关
        findViewById<Switch>(R.id.switchBootStart)?.let { sw ->
            sw.isChecked = prefsManager.bootStart
            sw.setOnCheckedChangeListener { _, checked ->
                prefsManager.bootStart = checked
            }
        }
    }

    private fun setupSingleClickSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinnerSingleClick) ?: return
        
        val options = arrayOf("返回桌面", "无操作")
        val values = arrayOf("home", "none")
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        
        // 设置当前值
        val currentValue = prefsManager.keyMappingSingleClick
        val currentindex = values.indexOf(currentValue)
        if (currentindex >= 0) {
            spinner.setSelection(currentindex)
        }
        
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                prefsManager.keyMappingSingleClick = values[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun requestLocation() {
        Toast.makeText(this, "正在IP定位...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch {
            try {
                val resp = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.andesk.launcher.data.remote.AmapApiHolder.api.ipLocation().execute()
                }
                val city = resp.body()?.city?.replace("市", "")
                if (!city.isNullOrEmpty()) {
                    updateCityFromIp(city, resp.body()?.adcode ?: "")
                } else {
                    Toast.makeText(this@SettingsActivity, "定位失败，请手动选择", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "定位失败，请手动选择", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCityFromIp(city: String, adcode: String) {
        val result = CityData.find(city)
        if (result != null) {
            val (prov, cityObj) = result
            prefsManager.weatherCity = cityObj.name
            prefsManager.weatherCityId = adcode
            prefsManager.clearWeatherCache()
            // 更新省
            findViewById<Spinner>(R.id.spinnerProvince)?.let { sp ->
                sp.setSelection(CityData.provinces.indexOf(prov).coerceAtLeast(0))
            }
            // 省切换后等city重建，再选市
            findViewById<Spinner>(R.id.spinnerCity)?.postDelayed({
                val cs = findViewById<Spinner>(R.id.spinnerCity)
                cs?.adapter?.let { a ->
                    for (i in 0 until a.count) {
                        if (a.getItem(i).toString() == cityObj.name) { cs.setSelection(i); break }
                    }
                }
            }, 300)
            Toast.makeText(this, "IP定位: ${cityObj.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "定位失败，请手动选择", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateKeyMappingVisibility(enabled: Boolean) {
        findViewById<View>(R.id.layoutSingleClick)?.alpha = if (enabled) 1.0f else 0.5f
        findViewById<View>(R.id.layoutShowToast)?.alpha = if (enabled) 1.0f else 0.5f
        
        findViewById<Spinner>(R.id.spinnerSingleClick)?.isEnabled = enabled
        findViewById<Switch>(R.id.switchShowToast)?.isEnabled = enabled
    }

    private fun loadSettings() {
        findViewById<Switch>(R.id.switch24Hour)?.isChecked = prefsManager.is24HourFormat
        findViewById<Switch>(R.id.switchWeather)?.isChecked = prefsManager.showWeather
        
        // 按键映射设置
        findViewById<Switch>(R.id.switchKeyMapping)?.isChecked = prefsManager.keyMappingEnabled
        findViewById<Switch>(R.id.switchShowToast)?.isChecked = prefsManager.keyMappingShowToast
        updateKeyMappingVisibility(prefsManager.keyMappingEnabled)
    }

    private fun startKeyMappingService() {
        if (!KeyMappingService.isRunning) {
            try {
                val intent = Intent(this, KeyMappingService::class.java)
                startForegroundService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopKeyMappingService() {
        if (KeyMappingService.isRunning) {
            try {
                val intent = Intent(this, KeyMappingService::class.java)
                stopService(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "请找到'安云桌面按键映射'并开启", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "无法打开无障碍设置", Toast.LENGTH_SHORT).show()
        }
    }
}
