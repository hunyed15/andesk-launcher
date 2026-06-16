# 安云桌面 (AnDesk Launcher)

面向大屏 Android 设备的轻量级桌面启动器，主要用于云笔电、电视盒子、平板和类桌面场景。

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-green.svg)](https://developer.android.com)
[![API](https://img.shields.io/badge/API-26%2B-blue.svg)](https://developer.android.com)

## 当前版本

`v1.0.3`

## 项目定位

安云桌面不是手机 Launcher，而是为横屏、大屏、键盘鼠标设备做的桌面入口。它优先保证常用信息可扫读、应用打开路径短、默认桌面行为稳定。

适用场景：

- 云笔电、云电脑终端
- Android 平板和二合一设备
- 电视盒子、横屏大屏设备
- 需要键盘操作的 Android 桌面环境

## 功能概览

| 功能 | 当前状态 |
| --- | --- |
| 默认桌面 | 支持注册为 Android HOME Launcher |
| 顶部搜索 | 点击左上角搜索胶囊进入应用抽屉搜索 |
| 顶部控制区 | WiFi、电池百分比、时间、清理内存、设置、主题、电源菜单 |
| 时钟天气 | 左侧信息卡显示时间、日期、天气图标、城市、温度范围和天气现象 |
| 高德天气 | 使用实况天气和预报天气，温度范围来自预报数据 |
| 今日诗词 | 支持展示和手动刷新 |
| 桌面分页 | 首页 2 行应用，后续页更多应用，支持页码点击 |
| 应用排序 | 中文名称优先，中文应用按中文排序 |
| 应用抽屉 | 支持所有应用浏览和搜索 |
| Dock 栏 | 4 个常用应用位，空位显示添加入口 |
| 文件夹 | 支持拖拽创建、打开和管理 |
| 明暗主题 | 浅色和深色主题资源已适配主要桌面组件 |
| Win 键映射 | 单击 Win 返回安云桌面，组合键和双击保留给系统 |
| 电源菜单 | 右上角电源按钮调用系统电源菜单，依赖无障碍服务 |

## 首页布局

当前首页是横屏桌面布局：

```text
┌────────────────────────────────────────────────────────────────────┐
│ 搜索应用                                      WiFi 电池 时间 清理 设置 主题 电源 │
├───────────────────────┬────────────────────────────────────────────┤
│ 时间 / 日期 / 天气       │ 桌面应用分页区域                              │
│ 今日诗词                │ 1  2  3  4                                  │
├───────────────────────┴────────────────────────────────────────────┤
│                         Dock 常用应用 + 所有应用                    │
└────────────────────────────────────────────────────────────────────┘
```

顶部右侧按钮说明：

| 按钮 | 行为 |
| --- | --- |
| WiFi | 打开系统 WiFi 设置 |
| 电池 | 打开系统电池设置，并在顶部显示电量百分比 |
| 清理 | 执行一次内存清理并显示当前内存占用 |
| 设置 | 打开安云桌面设置页 |
| 主题 | 在浅色和深色模式之间切换 |
| 电源 | 调用系统电源菜单，需开启无障碍服务 |

## 按键映射说明

按键映射通过 `KeyMappingAccessibilityService` 实现，需要在系统无障碍设置里开启“安云桌面按键映射”。

当前策略：

- 单击 Win 键：返回安云桌面。
- Win 组合键：不拦截，继续交给系统。
- Win 双击：不触发桌面，继续交给系统。
- 物理电源键：不做映射。Android 通常会在系统层处理电源键，普通 APK 无法可靠接管。

右上角电源按钮仍然保留，它通过无障碍服务调用系统电源菜单。

## 天气说明

天气数据来自高德天气接口：

- 实况天气：城市、天气现象、实时温度、湿度、风力等。
- 预报天气：当天到未来几天的白天/夜间天气和温度。
- 桌面温度范围显示为 `夜间低温°~白天高温°`。

当前项目中高德 Key 是硬编码在代码里的，位置：

```text
app/src/main/java/com/andesk/launcher/data/remote/AmapWeatherApi.kt
```

如果后续要正式分发，建议迁移到 `local.properties` + `BuildConfig`，避免把私有 Key 提交到公开仓库。Android 原生项目通常不直接使用 Web 项目那种 `.env`。

## 构建与安装

环境要求：

- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 34
- Android 8.0 及以上设备

构建方式：

```text
Android Studio -> Build -> Build Bundle(s) / APK(s) -> Build APK(s)
```

安装调试：

```bat
adb install -r -d -g app-debug.apk
```

设置为默认桌面：

```bat
adb shell cmd package set-home-activity com.andesk.launcher.debug/com.andesk.launcher.ui.home.HomeActivity
adb shell input keyevent KEYCODE_HOME
```

检查当前默认桌面：

```bat
adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

## 项目结构

```text
app/src/main/java/com/andesk/launcher/
├── App.kt
├── data/
│   ├── local/          # SharedPreferences、文件夹等本地数据
│   ├── model/          # App、天气、文件夹模型
│   ├── remote/         # 高德天气、今日诗词接口
│   └── repository/     # 应用和天气仓库
├── receiver/           # 开机、应用安装卸载、屏幕状态广播
├── service/            # 按键映射前台服务和无障碍服务
├── ui/
│   ├── appdrawer/      # 应用抽屉和搜索
│   ├── home/           # 桌面首页、分页、Dock、文件夹
│   └── settings/       # 设置页
└── util/               # 设备、内存、图片等工具
```

## 更新日志

### v1.0.3 (2026-06-16)

- 优化 Win 键映射：单击返回桌面，组合键和双击不再被拦截。
- 保留右上角电源按钮，点击打开系统电源菜单。
- 新增顶部电池百分比显示。
- 优化时钟、日期、天气卡片布局。
- 天气改为高德实况 + 预报组合，支持温度范围显示。
- 优化浅色主题下顶部控制区、Dock、页码和桌面组件的视觉适配。
- 应用排序改为中文名称优先。
- 修复桌面分页切换时部分图标重叠的问题。

### v1.0.2

- 完善桌面首页视觉细节。
- 增加 WiFi 设置入口。
- 增加应用抽屉搜索入口。
- 改进应用分页、Dock 和文件夹交互。

### v1.0.0

- 初始版本。
- 支持桌面时钟、天气、诗词、应用网格、Dock、应用抽屉、文件夹、拖拽排序、明暗主题和按键映射。

## 许可证

本项目基于 MIT 许可证开源，详见 [LICENSE](LICENSE)。

## 项目链接

[https://github.com/hunyed15/andesk-launcher](https://github.com/hunyed15/andesk-launcher)
