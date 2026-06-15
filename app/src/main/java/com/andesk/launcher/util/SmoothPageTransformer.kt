package com.andesk.launcher.util

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class SmoothPageTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val absPos = Math.abs(position)
        page.translationX = 0f
        page.translationY = 0f
        page.scaleX = 1f
        page.scaleY = 1f
        page.alpha = 1f - absPos.coerceAtMost(1f) * 0.08f
    }
}
