# 安云桌面 (AnDesk Launcher)

> 面向大屏安卓设备的轻量级桌面启动器，专为云笔电、二合一平板、安卓平板设计。

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-26%2B-blue.svg)](https://developer.android.com)

---

## 📱 项目简介

安云桌面是一款专为大屏安卓设备设计的轻量级桌面启动器。针对移动云笔电、二合一平板、安卓平板等16:9屏幕设备进行了优化，提供简洁高效的桌面体验。

### 设计理念

- **简洁优先**：去除花里胡哨，专注核心功能
- **大屏优化**：针对16:9屏幕优化布局
- **键盘友好**：支持键盘快捷操作
- **轻量高效**：支持Android Go设备（2GB内存）

---

## ✨ 核心功能

| 功能 | 说明 |
|------|------|
| **时钟+天气** | 左上角显示，实时更新，温度范围显示 |
| **诗词** | 右上角古诗词，支持手动/自动刷新 |
| **应用网格** | 2行×5列布局，支持分页和拖拽排序 |
| **Dock栏** | 4个常用应用 + 所有应用按钮 |
| **应用抽屉** | macOS风格，毛玻璃透明背景 |
| **文件夹** | 拖拽创建，支持重命名 |
| **清理内存** | 右上角小火箭一键清理 |
| **按键映射** | Win键单击/双击自定义功能 |
| **主题切换** | 明暗模式一键切换 |

---

## 🎨 布局设计

### 整体布局结构

屏幕分为四个主要区域：

```
┌─────────────────────────────────────────────────────────────────┐
│  区域1：顶部栏（右上角）                                         │
│                                              [🚀清理] [设置] [主题] │
├───────────────────────────────┬───────────────────────────────┤
│  区域2：信息展示区（上半部分）                                   │
│  ┌───────────────────────┐   ┌───────────────────────┐       │
│  │  左半边50%            │   │  右半边50%            │       │
│  │  20:28                │   │  "生活不止眼前的     │       │
│  │                       │   │    苟且..."          │       │
│  │  ☀️ 22°~33°  上海    │   │           ——高晓松   │       │
│  │                       │   │              [🔄]    │       │
│  │  06月13日             │   │                       │       │
│  │  周六 四月二八        │   │                       │       │
│  └───────────────────────┘   └───────────────────────┘       │
│                               ↑ 各占50%宽度                   │
├───────────────────────────────────────────────────────────────┤
│  区域3：应用区（下半部分）                                      │
│  [App1] [App2] [App3] [App4] [App5]                          │
│  [App6] [App7] [App8] [App9] [App10]                         │
│              (1) (2) (3) (4)  ← 页码指示器（可点击翻页）       │
├───────────────────────────────────────────────────────────────┤
│  区域4：Dock栏（底部居中）                                      │
│  [Dock1] [Dock2] [Dock3] [Dock4] │ [所有应用]                 │
└───────────────────────────────────────────────────────────────┘
```

---

### 区域1：顶部栏

**位置**：右上角

**内容**：功能按钮（从左到右）

```
[🚀清理] [设置] [主题切换]
```

| 按钮 | 功能 | 图标样式 |
|------|------|----------|
| 清理内存 | 一键清理后台应用 | 小火箭图标，描边阴影 |
| 设置 | 打开桌面设置 | 齿轮图标，描边阴影 |
| 主题切换 | 明暗模式切换 | 太阳/月亮图标，描边阴影 |

**设计规范**：
- 按钮大小：30dp
- 按钮间距：14dp
- 背景：描边阴影效果（半透明深色 + 白色描边）
- 距顶部：20dp
- 距右侧：28dp

---

### 区域2：信息展示区

**位置**：屏幕上半部分

**布局**：左右各占50%宽度，各自居中

---

#### 左半边：时钟+天气组件

```
┌───────────────────────┐
│  20:28           ☀️  │  ← 第1行: 时间65% | 天气图标35%
│  68sp           32sp  │
│                       │
│  06月14日        上海 │  ← 第2行: 日期 | 城市
│  14sp           14sp  │     baseline对齐
│                       │
│  周六 四月二八 22°~33°│  ← 第3行: 农历 | 温度
│  12sp          18sp   │     baseline对齐
└───────────────────────┘
```

**布局说明**：
- 三行两列网格：左列（时间→日期→农历），右列（图标→城市→温度）
- 列宽比例 65%/35%，每行 baseline 对齐
- 距顶部：80dp

**组件尺寸**：

| 元素 | 大小 | 说明 |
|------|------|------|
| 时间 | 68sp | Light字重，-0.02字间距 |
| 天气图标 | 32sp | Emoji，右对齐 |
| 日期 | 14sp | MM月dd日，左对齐 |
| 城市 | 14sp | 右对齐 |
| 农历 | 12sp | 星期+农历 |
| 温度 | 18sp | 最低°~最高°，右对齐 |

---

#### 右半边：今日诗词组件

```
┌───────────────────────┐
│                 [🔄]  │
│  "生活不止眼前的      │
│    苟且，还有诗       │
│    和远方。"          │
│                       │
│        ——高晓松      │
└───────────────────────┘
```

**布局说明**：
- 内容居中显示
- 刷新按钮在边框右上角
- 来源右对齐，从句子末尾开始
- 距顶部：80dp（与左侧对齐）

**组件尺寸**：

| 元素 | 大小 | 说明 |
|------|------|------|
| 句子 | 17sp | 斜体，最多2行 |
| 来源 | 13sp | 斜体，右对齐 |
| 刷新按钮 | 28dp | 圆形半透明背景 |

**间距规范**：
- 句子与来源间距：4dp
- 内容区域内边距：20dp
- 刷新按钮距边框：8dp

---

### 区域3：应用网格区

**位置**：信息展示区下方

**布局**：2行×5列，支持分页

```
[App1] [App2] [App3] [App4] [App5]
[App6] [App7] [App8] [App9] [App10]
```

**分页规则**：
- 首页：2行×5列 = 10个应用
- 后续页：5行×5列 = 25个应用
- 超出的应用自动分页

**组件尺寸**：

| 元素 | 大小 | 说明 |
|------|------|------|
| 应用图标 | 56dp | 16dp圆角 |
| 图标内边距 | 12dp | |
| 应用名称 | 12sp | 最多1行，超出省略 |
| 网格间距 | 24dp | 水平和垂直 |

**交互功能**：
- 点击：打开应用
- 长按：进入编辑模式（图标抖动）
- 拖拽：移动应用位置
- 拖拽到一起：创建文件夹

---

#### 页码指示器

```
(1) (2) (3) (4)
```

**设计规范**：
- 圆形按钮，24dp大小
- 当前页：蓝色背景白色文字
- 非当前页：半透明白色背景灰色文字
- 支持点击跳转
- 鼠标悬停时放大效果

---

### 区域4：Dock栏

**位置**：底部居中

**布局**：4个常用应用 + 分隔线 + 所有应用按钮

```
[Dock1] [Dock2] [Dock3] [Dock4] │ [所有应用]
```

**组件尺寸**：

| 元素 | 大小 | 说明 |
|------|------|------|
| Dock按钮 | 52dp | 16dp圆角背景 |
| 应用图标 | 40dp | centerCrop缩放 |
| 分隔线 | 1dp×40dp | 淡灰色 |
| 所有应用按钮 | 52dp | 网格图标 |

**Dock栏容器**：
- 背景：白色/深色表面色
- 圆角：24dp
- 阴影：8dp
- 描边：1dp淡灰色
- 内边距：20dp水平，8dp垂直
- 距底部：16dp

**交互功能**：
- 点击：打开应用
- 长按：弹出应用选择器，更换Dock应用
- 默认应用：电话、短信、浏览器、设置

---

### 应用抽屉布局

**触发**：点击Dock栏"所有应用"按钮

**样式**：macOS风格，毛玻璃透明背景

```
┌─────────────────────────────────────────────────────────┐
│  [←返回]  所有应用                                       │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 搜索应用...                                     │   │
│  ├─────────────────────────────────────────────────┤   │
│  │  [图标]  [图标]  [图标]  [图标]  [图标]  [图标] │   │
│  │   微信     QQ     抖音    淘宝    百度    高德   │   │
│  │                                                 │   │
│  │  [图标]  [图标]  [图标]  [图标]  [图标]  [图标] │   │
│  │  支付宝   WPS    B站    网易云   知乎    钉钉   │   │
│  └─────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

**布局规格**：
- 面板宽度：屏幕宽度 - 160dp（左右各80dp）
- 面板高度：屏幕高度 - 120dp（上下各60dp）
- 圆角：20dp
- 背景：`#E6F0F0F0`（90%不透明度）
- 应用图标：64dp，16dp圆角
- 网格列数：6列
- 搜索框：顶部

---

### 文件夹布局

**触发**：拖拽应用到另一个应用上

```
┌─────────────────────┐
│      文件夹名称      │
│  ┌─────┐  ┌─────┐  │
│  │App1 │  │App2 │  │
│  └─────┘  └─────┘  │
│  ┌─────┐  ┌─────┐  │
│  │App3 │  │App4 │  │
│  └─────┘  └─────┘  │
│                     │
│  [重命名] [删除]    │
└─────────────────────┘
```

**功能**：
- 显示文件夹内前4个应用图标（2×2网格）
- 点击应用：打开应用
- 长按应用：从文件夹移除
- 重命名：修改文件夹名称
- 删除：解散文件夹，应用返回桌面

---

## 🎨 设计规范

### 颜色系统（Material You）

#### 浅色模式

| 用途 | 颜色值 |
|------|--------|
| 主色调 | `#355db4` |
| 背景 | `#f7f6fa` |
| 表面 | `#ffffff` |
| 文字 | `#1b1b1f` |
| 次要文字 | `#5d5c63` |

#### 深色模式

| 用途 | 颜色值 |
|------|--------|
| 主色调 | `#b2c5ff` |
| 背景 | `#121316` |
| 表面 | `#1e1e23` |
| 文字 | `#e4e1e6` |
| 次要文字 | `#c5c2c9` |

### 圆角规范

| 元素 | 圆角 |
|------|------|
| 卡片 | 16dp |
| 按钮 | 12dp |
| 输入框 | 8dp |
| Dock栏 | 24dp |
| 应用图标 | 16dp |
| 文件夹 | 12dp |

### 字体规范

| 元素 | 大小 | 字重 |
|------|------|------|
| 时间 | 68sp | Light |
| 日期 | 14sp | Regular |
| 天气温度 | 18sp | Medium |
| 一言 | 13sp | Italic |
| 应用名称 | 12sp | Regular |

### 阴影规范

| 元素 | 阴影 |
|------|------|
| Dock栏 | 8dp |
| 顶部按钮 | 描边阴影 |
| 对话框 | 4dp |
| Toast | 8dp |

---

## 🛠️ 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 语言 | Kotlin | Android官方推荐 |
| UI | XML + View | 比Compose更省内存 |
| 图片加载 | Coil | Kotlin优先，轻量级 |
| 网络 | Retrofit + OkHttp | API请求 |
| 本地存储 | SharedPreferences | 轻量级配置存储 |
| JSON | Gson + org.json | 数据序列化 |
| 后台任务 | WorkManager | 定时任务 |

### 项目结构

```
app/src/main/java/com/andesk/launcher/
├── App.kt                          # Application类
├── ui/
│   ├── home/
│   │   ├── HomeActivity.kt         # 主桌面
│   │   ├── AppPageAdapter.kt       # 分页适配器
│   │   ├── AppGridAdapter.kt       # 应用网格适配器
│   │   ├── DesktopGridAdapter.kt   # 桌面项适配器
│   │   └── FolderDialog.kt         # 文件夹弹窗
│   ├── appdrawer/
│   │   ├── AppDrawerActivity.kt    # 应用抽屉
│   │   └── AppDrawerAdapter.kt     # 应用列表适配器
│   └── settings/
│       └── SettingsActivity.kt     # 设置页面
├── data/
│   ├── model/
│   │   ├── AppInfo.kt              # 应用信息
│   │   ├── WeatherInfo.kt          # 天气信息
│   │   └── Folder.kt               # 文件夹模型
│   ├── repository/
│   │   ├── AppRepository.kt        # 应用仓库
│   │   └── WeatherRepository.kt    # 天气仓库
│   ├── local/
│   │   ├── PrefsManager.kt         # 偏好设置
│   │   └── FolderManager.kt        # 文件夹管理
│   └── remote/
│       ├── WeatherService.kt       # 天气API
│       ├── HitokotoService.kt      # 一言API
│       └── HitokotoClient.kt       # 一言客户端
├── receiver/
│   ├── BootReceiver.kt             # 开机启动
│   ├── PackageReceiver.kt          # 应用安装/卸载
│   └── ScreenReceiver.kt           # 屏幕解锁
├── service/
│   ├── KeyMappingService.kt        # 按键映射服务
│   └── KeyMappingAccessibilityService.kt  # 无障碍服务
└── util/
    ├── DeviceUtils.kt              # 设备工具
    ├── MemoryUtils.kt              # 内存工具
    └── AppUtils.kt                 # 应用工具
```

---

## 📦 依赖库

```gradle
// AndroidX Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("androidx.activity:activity-ktx:1.8.2")

// UI Components
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")
implementation("androidx.viewpager2:viewpager2:1.0.0")

// Material Design
implementation("com.google.android.material:material:1.11.0")

// Network
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// Image Loading
implementation("io.coil-kt:coil:2.5.0")

// JSON
implementation("com.google.code.gson:gson:2.10.1")
```

---

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/hunyed15/andesk-launcher.git
   cd andesk-launcher
   ```

2. **用Android Studio打开**
   - 打开 Android Studio
   - 选择 `File > Open`
   - 选择 `andesk-launcher` 文件夹

3. **构建项目**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```

4. **安装到设备**
   - 连接安卓设备
   - 点击 Run 按钮

---

## 📐 布局适配

### 屏幕尺寸适配

| 设备类型 | 屏幕尺寸 | 图标列数 |
|---------|----------|---------|
| 手机 | < 10寸 | 4列 |
| 小平板 | 10寸 | 5列 |
| 大平板/云笔电 | 12-14寸 | 6列 |

### 分辨率支持

| 分辨率 | 适配情况 |
|--------|---------|
| 1920×1200 | ✅ 最佳体验 |
| 1920×1080 | ✅ 良好支持 |
| 1280×800 | ✅ 基本支持 |

---

## 🔧 配置说明

### wttr.in 天气API

- 免费: https://wttr.in，无需注册，无调用限制

### 默认应用设置

在 `PrefsManager.kt` 中修改默认Dock栏应用：
```kotlin
private fun getDefaultDockApps(): List<String> {
    return listOf(
        "com.android.dialer",      // 电话
        "com.android.mms",         // 短信
        "com.android.chrome",      // 浏览器
        "com.android.settings"     // 设置
    )
}
```

---

## 📝 更新日志

### v1.0.0 (2026-06-13)

- 🎉 初始版本发布
- ✅ 主桌面时钟+天气组件
- ✅ 一言随机句子
- ✅ 应用网格分页
- ✅ Dock栏应用管理
- ✅ 应用抽屉（macOS风格）
- ✅ 文件夹功能
- ✅ 拖拽排序
- ✅ 按键映射
- ✅ 清理内存
- ✅ 明暗主题切换

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建你的分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的改动 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

---

## 📄 许可证

本项目基于 MIT 许可证开源 - 详见 [LICENSE](LICENSE) 文件

---

## 🙏 致谢

- [wttr.in](https://wttr.in) - 天气数据API
- [今日诗词](https://www.jinrishici.com/) - 古诗词API
- [Material Design](https://m3.material.io/) - 设计规范
- [当贝桌面](https://www.dangbei.com/) - 设计灵感

---

## 📧 联系方式

- 项目链接: [https://github.com/hunyed15/andesk-launcher](https://github.com/hunyed15/andesk-launcher)
- 问题反馈: [Issues](https://github.com/hunyed15/andesk-launcher/issues)
