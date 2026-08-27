package com.andesk.launcher.util

import android.content.Context
import android.provider.Settings

/**
 * 动画相关工具：尊重系统"关闭动画 / Reduce Motion"设置。
 *
 * Android 没有 iOS 那样的公开 Reduce Motion 开关，业界通常用
 * `Settings.Global.ANIMATOR_DURATION_SCALE`（开发者选项 → 动画程序时长缩放 = 0）
 * 来判断用户已关闭系统动画。此值可在运行时被用户改动，因此每次需要时实时读取。
 */
object MotionUtils {

    /**
     * 用户是否已关闭系统动画（减少动态效果）。
     *
     * 用于在"编辑模式抖动"这类循环动画处跳过，尊重对动态效果敏感的用户
     * （参考 HIG Accessibility：Reduce Motion）。
     *
     * @return true 表示应减少/跳过重复动画
     */
    fun isAnimationDisabled(context: Context): Boolean {
        return try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }
}
