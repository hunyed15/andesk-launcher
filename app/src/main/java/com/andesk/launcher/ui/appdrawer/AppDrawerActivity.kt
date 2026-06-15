package com.andesk.launcher.ui.appdrawer

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andesk.launcher.R
import com.andesk.launcher.data.local.PrefsManager
import com.andesk.launcher.data.model.AppInfo
import com.andesk.launcher.data.repository.AppRepository

class AppDrawerActivity : AppCompatActivity() {

    private lateinit var prefsManager: PrefsManager
    private lateinit var appRepository: AppRepository
    private lateinit var adapter: AppDrawerAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupFullScreen()
        setContentView(R.layout.activity_app_drawer)

        prefsManager = PrefsManager(this)
        appRepository = AppRepository(this, prefsManager)

        initViews()
        setupRecyclerView()
        loadApps()
        
        // 搜索胶囊打开时自动聚焦搜索框
        if (intent.getBooleanExtra("openSearch", false)) {
            searchView.requestFocus()
            searchView.isIconified = false
        }
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
        recyclerView = findViewById(R.id.rvAllApps)
        searchView = findViewById(R.id.searchView)

        // 返回按钮
        findViewById<View>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // 搜索
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterApps(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterApps(newText)
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = AppDrawerAdapter { appInfo ->
            appRepository.launchApp(appInfo.packageName)
            finish()
        }
        
        recyclerView.apply {
            layoutManager = GridLayoutManager(this@AppDrawerActivity, 6)
            adapter = this@AppDrawerActivity.adapter
        }
    }

    private fun loadApps() {
        allApps = appRepository.getInstalledApps()
        adapter.submitList(allApps)
    }

    private fun filterApps(query: String?) {
        if (query.isNullOrEmpty()) {
            adapter.submitList(allApps)
        } else {
            val filtered = allApps.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
            adapter.submitList(filtered)
        }
    }
}
