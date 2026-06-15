package com.andesk.launcher.data.local

import android.content.Context
import android.content.SharedPreferences
import com.andesk.launcher.data.model.Folder
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

/**
 * 文件夹管理器
 */
class FolderManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "andesk_folders"
        private const val KEY_FOLDERS = "folders"
        private const val KEY_APP_ORDER = "app_order"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * 获取所有文件夹
     */
    fun getAllFolders(): List<Folder> {
        val json = prefs.getString(KEY_FOLDERS, null)
        return if (json != null) {
            try {
                val jsonArray = JSONArray(json)
                val result = mutableListOf<Folder>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val appsArray = obj.getJSONArray("apps")
                    val apps = mutableListOf<String>()
                    for (j in 0 until appsArray.length()) {
                        apps.add(appsArray.getString(j))
                    }
                    result.add(
                        Folder(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            apps = apps,
                            position = obj.optInt("position", 0)
                        )
                    )
                }
                result
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * 保存文件夹列表
     */
    fun saveFolders(folders: List<Folder>) {
        val json = gson.toJson(folders)
        prefs.edit().putString(KEY_FOLDERS, json).apply()
    }

    /**
     * 创建新文件夹
     */
    fun createFolder(name: String, apps: List<String> = emptyList()): Folder {
        val folder = Folder(
            id = "folder_${System.currentTimeMillis()}",
            name = name,
            apps = apps.toMutableList()
        )
        
        val folders = getAllFolders().toMutableList()
        folders.add(folder)
        saveFolders(folders)
        
        return folder
    }

    /**
     * 删除文件夹
     */
    fun deleteFolder(folderId: String) {
        val folders = getAllFolders().toMutableList()
        folders.removeAll { it.id == folderId }
        saveFolders(folders)
    }

    /**
     * 更新文件夹
     */
    fun updateFolder(folder: Folder) {
        val folders = getAllFolders().toMutableList()
        val index = folders.indexOfFirst { it.id == folder.id }
        if (index >= 0) {
            folders[index] = folder
            saveFolders(folders)
        }
    }

    /**
     * 重命名文件夹
     */
    fun renameFolder(folderId: String, newName: String) {
        val folders = getAllFolders().toMutableList()
        val folder = folders.find { it.id == folderId }
        if (folder != null) {
            val updatedFolder = folder.copy(name = newName)
            val index = folders.indexOf(folder)
            folders[index] = updatedFolder
            saveFolders(folders)
        }
    }

    /**
     * 添加应用到文件夹
     */
    fun addAppToFolder(folderId: String, packageName: String) {
        val folders = getAllFolders().toMutableList()
        val folder = folders.find { it.id == folderId }
        if (folder != null) {
            folder.addApp(packageName)
            saveFolders(folders)
        }
    }

    /**
     * 从文件夹移除应用
     */
    fun removeAppFromFolder(folderId: String, packageName: String) {
        val folders = getAllFolders().toMutableList()
        val folder = folders.find { it.id == folderId }
        if (folder != null) {
            folder.removeApp(packageName)
            if (folder.isEmpty()) {
                folders.remove(folder)
            }
            saveFolders(folders)
        }
    }

    /**
     * 获取应用所在的文件夹
     */
    fun getFolderForApp(packageName: String): Folder? {
        return getAllFolders().find { it.apps.contains(packageName) }
    }

    /**
     * 保存应用排序
     */
    fun saveAppOrder(appOrder: List<String>) {
        val jsonArray = JSONArray()
        appOrder.forEach { jsonArray.put(it) }
        prefs.edit().putString(KEY_APP_ORDER, jsonArray.toString()).apply()
    }

    /**
     * 获取应用排序
     */
    fun getAppOrder(): List<String> {
        val json = prefs.getString(KEY_APP_ORDER, null)
        return if (json != null) {
            try {
                val jsonArray = JSONArray(json)
                val result = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    result.add(jsonArray.getString(i))
                }
                result
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }
}
