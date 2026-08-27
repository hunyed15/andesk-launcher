package com.andesk.launcher.util

import android.view.KeyEvent

/**
 * 按键映射：触发键选项与工具。
 *
 * 原实现把"返回到桌面"的触发键硬编码为 Win 键（左右 Meta）。
 * 现在支持可配置触发键（预置 + 自定义录制），这里统一维护键码与名称。
 */
object KeyMappingKeys {

    /** 预置可选触发键：键码 to 显示名 */
    val presets: List<Pair<Int, String>> = listOf(
        KeyEvent.KEYCODE_META_LEFT to "Win 键",
        KeyEvent.KEYCODE_F11 to "F11",
        KeyEvent.KEYCODE_F12 to "F12",
        KeyEvent.KEYCODE_F10 to "F10",
        KeyEvent.KEYCODE_F5 to "F5",
        KeyEvent.KEYCODE_F6 to "F6",
        KeyEvent.KEYCODE_ESCAPE to "Esc",
        KeyEvent.KEYCODE_HOME to "Home",
        KeyEvent.KEYCODE_MENU to "Menu",
        KeyEvent.KEYCODE_TAB to "Tab",
        KeyEvent.KEYCODE_BACK to "Back"
    )

    fun presetCodes(): IntArray = presets.map { it.first }.toIntArray()

    /** 实际按键是否命中当前触发键（Win 键 = 左右 Meta 都算） */
    fun matches(savedCode: Int, keyCode: Int): Boolean {
        if (savedCode == KeyEvent.KEYCODE_META_LEFT || savedCode == KeyEvent.KEYCODE_META_RIGHT) {
            return keyCode == KeyEvent.KEYCODE_META_LEFT || keyCode == KeyEvent.KEYCODE_META_RIGHT
        }
        return keyCode == savedCode
    }

    /** 是否为修饰键（不适合单独作为触发键，录制时跳过） */
    fun isModifier(keyCode: Int): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT,
        KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_CTRL_RIGHT,
        KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT,
        KeyEvent.KEYCODE_META_LEFT, KeyEvent.KEYCODE_META_RIGHT,
        KeyEvent.KEYCODE_CAPS_LOCK, KeyEvent.KEYCODE_NUM_LOCK,
        KeyEvent.KEYCODE_SCROLL_LOCK -> true
        else -> false
    }

    /** 键码 → 显示名 */
    fun labelFor(keyCode: Int): String {
        if (keyCode == KeyEvent.KEYCODE_META_LEFT || keyCode == KeyEvent.KEYCODE_META_RIGHT) {
            return "Win 键"
        }
        return presets.firstOrNull { it.first == keyCode }?.second
            ?: KeyEvent.keyCodeToString(keyCode).removePrefix("KEYCODE_")
    }
}
