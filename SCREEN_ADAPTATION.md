# 屏幕适配指南

## 概述

安云桌面支持多种分辨率的屏幕，通过 `sw600dp`、`sw800dp`、`sw1200dp` 等资源限定符实现自适应布局。

---

## 支持的屏幕尺寸

| 屏幕类型 | 最小宽度 | 典型分辨率 | DPI | 资源目录 |
|---------|----------|-----------|-----|---------|
| 默认（手机） | < 600dp | 1080P (1920×1080) | ~160dpi | `values/` |
| 小平板 | ≥ 600dp | 1080P (1920×1200) | ~160dpi | `values-sw600dp/` |
| 大平板/云笔电 | ≥ 800dp | 1.5K (2560×1440) | ~240dpi | `values-sw800dp/` |
| 超大屏幕 | ≥ 1200dp | 2K/4K (3840×2160) | ~320dpi+ | `values-sw1200dp/` |

---

## 尺寸对照表

### 顶部栏

| 元素 | 默认 | sw600dp | sw800dp | sw1200dp |
|------|------|---------|---------|----------|
| 距顶部 | 20dp | 24dp | 28dp | 32dp |
| 距右侧 | 28dp | 32dp | 36dp | 40dp |
| 按钮大小 | 30dp | 32dp | 34dp | 36dp |

### 时钟+天气

| 元素 | 默认 | sw600dp | sw800dp | sw1200dp |
|------|------|---------|---------|----------|
| 时间大小 | 68sp | 80sp | 96sp | 112sp |
| 天气图标 | 32sp | 36sp | 40sp | 44sp |
| 温度大小 | 18sp | 20sp | 22sp | 24sp |
| 城市大小 | 14sp | 15sp | 16sp | 17sp |
| 日期行1 | 14sp | 15sp | 16sp | 18sp |
| 日期行2 | 12sp | 13sp | 14sp | 15sp |
| 距顶部 | 80dp | 100dp | 120dp | 140dp |

### 一言

| 元素 | 默认 | sw600dp | sw800dp | sw1200dp |
|------|------|---------|---------|----------|
| 句子大小 | 17sp | 19sp | 21sp | 23sp |
| 来源大小 | 13sp | 14sp | 15sp | 16sp |
| 最大宽度 | 340dp | 420dp | 500dp | 600dp |
| 刷新按钮 | 28dp | 32dp | 36dp | 40dp |

### 应用网格

| 元素 | 默认 | sw600dp | sw800dp | sw1200dp |
|------|------|---------|---------|----------|
| 图标大小 | 56dp | 64dp | 72dp | 80dp |
| 图标内边距 | 12dp | 14dp | 16dp | 18dp |
| 图标圆角 | 16dp | 16dp | 18dp | 20dp |
| 应用名称 | 12sp | 13sp | 14sp | 15sp |
| 网格间距 | 24dp | 28dp | 32dp | 36dp |

### 页码指示器

| 元素 | 默认 | sw600dp | sw800dp | sw1200dp |
|------|------|---------|---------|----------|
| 指示器大小 | 24dp | 28dp | 32dp | 36dp |
| 文字大小 | 10sp | 11sp | 12sp | 13sp |

### Dock栏

| 元素 | 默认 | sw600dp | sw800dp | sw1200dp |
|------|------|---------|---------|----------|
| 距底部 | 16dp | 20dp | 24dp | 28dp |
| 圆角 | 24dp | 24dp | 28dp | 32dp |
| 高度 | 52dp | 56dp | 60dp | 64dp |
| 图标大小 | 40dp | 44dp | 48dp | 52dp |
| 内边距 | 20dp | 24dp | 28dp | 32dp |

---

## 使用方法

### 在布局文件中引用

```xml
<TextView
    android:textSize="@dimen/clock_text_size"
    android:layout_marginTop="@dimen/info_section_margin_top" />
```

### 在代码中获取

```kotlin
val textSize = resources.getDimension(R.dimen.clock_text_size)
val marginTop = resources.getDimensionPixelSize(R.dimen.info_section_margin_top)
```

---

## 适配原则

### 1. 使用 dp 而非 px

- **dp**：密度无关像素，自动适应不同DPI
- **px**：像素，不同DPI下显示大小不同

### 2. 字体使用 sp

- **sp**：可缩放像素，会跟随系统字体大小设置
- 适用于所有文字内容

### 3. 使用相对布局

- ConstraintLayout：约束布局，相对定位
- 百分比宽度：`layout_constraintWidth_percent`

### 4. 避免硬编码

```xml
<!-- ❌ 错误 -->
<TextView android:textSize="68sp" />

<!-- ✅ 正确 -->
<TextView android:textSize="@dimen/clock_text_size" />
```

---

## 添加新的屏幕尺寸

如果需要支持其他屏幕尺寸：

1. 在 `res/` 下创建新的资源目录，如 `values-sw720dp/`
2. 创建 `dimens.xml` 文件
3. 定义该屏幕尺寸下的尺寸值

```xml
<!-- values-sw720dp/dimens.xml -->
<resources>
    <dimen name="clock_text_size">88sp</dimen>
    <dimen name="app_icon_size">68dp</dimen>
    <!-- ... -->
</resources>
```

---

## 测试建议

| 测试项 | 测试方法 |
|--------|---------|
| 不同分辨率 | 使用Android模拟器，设置不同分辨率 |
| 不同DPI | 使用Android模拟器，设置不同DPI |
| 横竖屏 | 旋转设备或模拟器 |
| 字体大小 | 系统设置中调整字体大小 |

---

## 常见问题

### Q: 为什么我的布局在某些设备上显示异常？

A: 检查是否使用了硬编码的像素值，应该使用dp和sp。

### Q: 如何添加新的屏幕尺寸支持？

A: 参考"添加新的屏幕尺寸"章节。

### Q: 为什么有些尺寸在不同设备上看起来一样大？

A: dp和sp会自动适应DPI，保持物理尺寸一致。

---

## 参考资料

- [Android屏幕适配指南](https://developer.android.com/training/multiscreen/screensizes)
- [支持不同屏幕密度](https://developer.android.com/training/multiscreen/screendensities)
- [资源限定符](https://developer.android.com/guide/topics/resources/providing-resources#AlternativeResources)
