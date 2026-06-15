package com.andesk.launcher.data.model

/**
 * 文件夹模型
 */
data class Folder(
    val id: String,
    val name: String,
    val apps: MutableList<String> = mutableListOf(),  // 包名列表
    val position: Int = 0
) {
    /**
     * 添加应用到文件夹
     */
    fun addApp(packageName: String) {
        if (!apps.contains(packageName)) {
            apps.add(packageName)
        }
    }

    /**
     * 从文件夹移除应用
     */
    fun removeApp(packageName: String) {
        apps.remove(packageName)
    }

    /**
     * 文件夹是否为空
     */
    fun isEmpty(): Boolean = apps.isEmpty()

    /**
     * 文件夹中的应用数量
     */
    fun getAppCount(): Int = apps.size
}
