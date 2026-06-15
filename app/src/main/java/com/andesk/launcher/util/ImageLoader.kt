package com.andesk.launcher.util

import android.content.Context
import android.graphics.drawable.Drawable
import coil.ImageLoader
import coil.request.ImageRequest
import coil.target.Target
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 图片加载工具类
 * 使用Coil进行图片加载
 */
object ImageLoader {

    /**
     * 预加载图片到内存缓存
     */
    fun preload(context: Context, url: String) {
        val imageLoader = coil.Coil.imageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
            .build()
        imageLoader.enqueue(request)
    }

    /**
     * 清除内存缓存
     */
    fun clearMemoryCache(context: Context) {
        val imageLoader = coil.Coil.imageLoader(context)
        imageLoader.memoryCache?.clear()
    }

    /**
     * 清除磁盘缓存
     */
    suspend fun clearDiskCache(context: Context) {
        val imageLoader = coil.Coil.imageLoader(context)
        imageLoader.diskCache?.clear()
    }

    /**
     * 获取缓存大小（字节）
     */
    fun getCacheSize(context: Context): Long {
        val imageLoader = coil.Coil.imageLoader(context)
        val diskCache = imageLoader.diskCache
        return diskCache?.size ?: 0
    }
}
