package com.andesk.launcher.ui.recent

import android.app.ActivityManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andesk.launcher.R
import com.andesk.launcher.util.MemoryUtils

class RecentAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentAppsAdapter
    private lateinit var tvMemoryInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        setContentView(R.layout.activity_recent)

        initViews()
        loadRunningApps()
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
        recyclerView = findViewById(R.id.rvRecentApps)
        tvMemoryInfo = findViewById(R.id.tvMemoryInfo)

        // 返回按钮
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // 全部关闭按钮
        findViewById<View>(R.id.btnClearAll)?.setOnClickListener {
            clearAllApps()
        }

        // 设置RecyclerView
        adapter = RecentAppsAdapter { packageName ->
            killApp(packageName)
            loadRunningApps()
        }
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@RecentAppsActivity)
            adapter = this@RecentAppsActivity.adapter
        }
    }

    private fun loadRunningApps() {
        val memoryInfo = MemoryUtils.getMemoryInfo(this)
        tvMemoryInfo.text = "内存: ${memoryInfo.usedMB}MB / ${memoryInfo.totalMB}MB (${memoryInfo.usagePercent}%)"

        val runningProcesses = MemoryUtils.getRunningProcesses(this)
        val recentApps = runningProcesses
            .filter { it.processName != packageName }
            .filter { it.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE }
            .map { process ->
                RecentApp(
                    processName = process.processName,
                    pid = process.pid,
                    importance = process.importance
                )
            }

        adapter.submitList(recentApps)
    }

    private fun killApp(packageName: String) {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses(packageName)
    }

    private fun clearAllApps() {
        val excluded = listOf(packageName)
        MemoryUtils.killBackgroundProcesses(this, excluded)
        loadRunningApps()
    }
}

data class RecentApp(
    val processName: String,
    val pid: Int,
    val importance: Int
)
