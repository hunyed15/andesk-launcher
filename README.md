# AnDesk Launcher

> 面向大屏安卓设备的简洁桌面启动器

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-26%2B-blue.svg)](https://developer.android.com)

## ✨ 特性

- 📱 **应用网格** - 6列布局，适合大屏设备
- 🕐 **时钟显示** - 大字体数字时钟
- 🌤️ **天气卡片** - 和风天气API，实时天气
- 🔘 **Home小圆点** - 无触屏设备的便捷导航
- 📋 **通知栏** - 快捷开关、通知管理
- 🧹 **内存清理** - 一键清理后台应用
- 🎨 **现代化UI** - 圆角、毛玻璃、深色主题

## 📱 支持设备

| 设备类型 | 屏幕尺寸 | 适配情况 |
|---------|----------|---------|
| 移动云笔电 | 12-14寸 | ✅ 最佳体验 |
| 二合一平板 | 11-13寸 | ✅ 良好支持 |
| 安卓平板 | 10-12寸 | ✅ 良好支持 |
| Android Go | 各尺寸 | ✅ 性能优化 |

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/andesk-launcher.git
   ```

2. **用Android Studio打开**
   - 打开 Android Studio
   - 选择 `File > Open`
   - 选择 `andesk-launcher` 文件夹

3. **配置API Key**
   - 注册 [和风天气](https://dev.qweather.com/) 账号
   - 获取API Key
   - 在 `app/src/main/java/com/andesk/launcher/data/remote/QWeatherApi.kt` 中替换：
     ```kotlin
     const val API_KEY = "YOUR_API_KEY_HERE"
     ```

4. **运行项目**
   - 连接安卓设备或启动模拟器
   - 点击 `Run 'app'` 按钮

### 设置为默认桌面

1. 按 `Home` 键
2. 选择 `AnDesk Launcher` 作为默认桌面
3. 或在 `设置 > 应用 > 默认应用 > 主屏幕应用` 中选择

## 📁 项目结构

```
andesk-launcher/
├── app/src/main/java/com/andesk/launcher/
│   ├── App.kt                    # Application类
│   ├── ui/                       # 界面层
│   │   ├── home/                 # 主桌面
│   │   ├── appdrawer/            # 应用抽屉
│   │   ├── recent/               # 最近应用
│   │   ├── floating/             # Home小圆点
│   │   └── settings/             # 设置
│   ├── data/                     # 数据层
│   │   ├── model/                # 数据模型
│   │   ├── repository/           # 数据仓库
│   │   ├── local/                # 本地存储
│   │   └── remote/               # 远程API
│   ├── receiver/                 # 广播接收器
│   └── util/                     # 工具类
├── app/src/main/res/             # 资源文件
│   ├── layout/                   # 布局
│   ├── drawable/                 # 图标、背景
│   ├── values/                   # 基础资源
│   ├── values-sw600dp/           # 10寸平板
│   └── values-sw800dp/           # 12寸+设备
└── build.gradle.kts              # 构建配置
```

## 🎨 设计规范

### 圆角
- 卡片: 16dp
- 按钮: 12dp
- 输入框: 8dp

### 颜色
- 主色调: `#2196F3` (Material Blue)
- 背景色: `#121212` (深色模式)
- 文字色: `#FFFFFF` (主要) / `#B0B0B0` (次要)

## ⚡ 性能优化

针对低端设备的优化：

| 优化项 | 策略 |
|-------|------|
| 内存 | 限制缓存大小，及时回收 |
| 图片 | Glide + RGB_565格式 |
| 启动 | 延迟初始化非关键组件 |
| 后台 | 最小化服务，按需启动 |

## 📝 开发计划

- [x] 项目搭建
- [ ] M1: 基础桌面 + 图标网格
- [ ] M2: 时钟 + 天气 + Dock栏
- [ ] M3: 通知栏 + 后台管理
- [ ] M4: Home小圆点 + 设置
- [ ] M5: 测试 + 发布

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建你的分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📄 许可证

本项目基于 MIT 许可证开源 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

- [和风天气](https://dev.qweather.com/) - 天气数据API
- [Material Design](https://m3.material.io/) - 设计规范
- [当贝桌面](https://www.dangbei.com/) - 设计灵感

## 📧 联系方式

- 项目链接: [https://github.com/yourusername/andesk-launcher](https://github.com/yourusername/andesk-launcher)
- 问题反馈: [Issues](https://github.com/yourusername/andesk-launcher/issues)
