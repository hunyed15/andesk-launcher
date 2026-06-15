package com.andesk.launcher.ui.task

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andesk.launcher.R

class TaskManagerActivity : AppCompatActivity() {

    private val lockedPkgs = mutableSetOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var adapter: TaskAdapter
    private lateinit var tvMem: TextView
    private lateinit var btnKill: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_manager)

        tvMem = findViewById(R.id.tvMemoryPercent)
        btnKill = findViewById(R.id.btnKillAll)

        adapter = TaskAdapter(lockedPkgs) { pkg ->
            try { (getSystemService(ACTIVITY_SERVICE) as ActivityManager).killBackgroundProcesses(pkg) } catch (_: Exception) {}
            refresh()
        }
        findViewById<RecyclerView>(R.id.rvTasks).apply {
            layoutManager = LinearLayoutManager(this@TaskManagerActivity)
            adapter = this@TaskManagerActivity.adapter
        }

        btnKill.setOnClickListener { doCleanup() }
        window.decorView.setOnClickListener { finish() }
        refresh()
    }

    private fun doCleanup() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        adapter.getItems().forEach { task ->
            if (task.pkg !in lockedPkgs) {
                try { am.killBackgroundProcesses(task.pkg) } catch (_: Exception) {}
            }
        }
        System.gc()
        btnKill.text = "✅ 已优化"
        btnKill.isEnabled = false
        handler.postDelayed({ btnKill.text = "一键清理"; btnKill.isEnabled = true }, 1500)
        refresh()
    }

    private fun refresh() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val pm = packageManager
        val tasks = am.runningAppProcesses?.mapNotNull { proc ->
            if (proc.importance >= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE
                && proc.processName != packageName
                && !proc.processName.contains("system")
                && !proc.processName.contains("android")) {
                val pkg = proc.pkgList.firstOrNull() ?: return@mapNotNull null
                val name = try { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() } catch (_: Exception) { pkg }
                TaskInfo(pkg, name)
            } else null
        }?.distinctBy { it.pkg }?.sortedBy { it.name } ?: emptyList()

        adapter.setItems(tasks)
        updateMemory(am)
    }

    private fun updateMemory(am: ActivityManager) {
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val pct = ((mi.totalMem - mi.availMem) * 100 / mi.totalMem).toInt()
        tvMem.text = "${pct}%"
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
    }
}

data class TaskInfo(val pkg: String, val name: String)

class TaskAdapter(
    private val locked: MutableSet<String>,
    private val onClose: (String) -> Unit
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    private var items = listOf<TaskInfo>()

    fun setItems(list: List<TaskInfo>) { items = list; notifyDataSetChanged() }
    fun getItems() = items

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.ivAppIcon)
        val name: TextView = v.findViewById(R.id.tvAppName)
        val btnLock: ImageButton = v.findViewById(R.id.btnLock)
        val btnClose: ImageButton = v.findViewById(R.id.btnClose)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_task_app, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val task = items[pos]
        h.name.text = task.name
        try { h.icon.setImageDrawable(h.itemView.context.packageManager.getApplicationIcon(task.pkg)) } catch (_: Exception) {}

        val locked = task.pkg in this.locked
        h.btnLock.setImageResource(if (locked) R.drawable.ic_cleanup else android.R.drawable.ic_menu_view)
        h.btnLock.setOnClickListener {
            if (task.pkg in this.locked) this.locked.remove(task.pkg) else this.locked.add(task.pkg)
            notifyItemChanged(pos)
        }
        h.btnClose.setOnClickListener { onClose(task.pkg) }
    }

    override fun getItemCount() = items.size
}
