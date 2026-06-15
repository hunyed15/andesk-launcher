package com.andesk.launcher.ui.home

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andesk.launcher.R
import com.andesk.launcher.data.model.AppInfo
import com.andesk.launcher.data.model.Folder
import com.andesk.launcher.data.repository.AppRepository

/**
 * 文件夹详情弹窗
 */
class FolderDialog(
    private val folder: Folder,
    private val allApps: List<AppInfo>,
    private val appRepository: AppRepository,
    private val onFolderUpdated: () -> Unit
) : DialogFragment() {

    private lateinit var adapter: FolderAppAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_folder, null)
        
        initViews(view)
        
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    private fun initViews(view: View) {
        val tvFolderName = view.findViewById<TextView>(R.id.tvFolderName)
        val rvFolderApps = view.findViewById<RecyclerView>(R.id.rvFolderApps)
        val btnRename = view.findViewById<Button>(R.id.btnRename)
        val btnDeleteFolder = view.findViewById<Button>(R.id.btnDeleteFolder)

        // 设置文件夹名称
        tvFolderName.text = folder.name

        // 获取文件夹中的应用
        val folderApps = folder.apps.mapNotNull { packageName ->
            allApps.find { it.packageName == packageName }
        }

        // 设置应用列表
        adapter = FolderAppAdapter(
            apps = folderApps,
            onAppClick = { appInfo ->
                appRepository.launchApp(appInfo.packageName)
                dismiss()
            },
            onRemoveClick = { appInfo ->
                appRepository.removeAppFromFolder(folder.id, appInfo.packageName)
                onFolderUpdated()
                dismiss()
            }
        )

        rvFolderApps.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = this@FolderDialog.adapter
        }

        // 重命名按钮
        btnRename.setOnClickListener {
            showRenameDialog()
        }

        // 删除文件夹按钮
        btnDeleteFolder.setOnClickListener {
            showDeleteConfirmDialog()
        }
    }

    private fun showRenameDialog() {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rename, null)
        val editText = view.findViewById<EditText>(R.id.etFolderName)
        editText.setText(folder.name)
        editText.selectAll()

        AlertDialog.Builder(requireContext())
            .setTitle("重命名文件夹")
            .setView(view)
            .setPositiveButton("确定") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    appRepository.renameFolder(folder.id, newName)
                    onFolderUpdated()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "名称不能为空", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("删除文件夹")
            .setMessage("确定删除文件夹\"${folder.name}\"吗？\n文件夹中的应用将返回桌面。")
            .setPositiveButton("删除") { _, _ ->
                appRepository.deleteFolder(folder.id)
                onFolderUpdated()
                dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
