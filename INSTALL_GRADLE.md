# 安装 Gradle

## Windows 安装方法

### 方法1：使用 Scoop（推荐）
```powershell
# 安装 Scoop（如果没有）
iwr -useb get.scoop.sh | iex

# 安装 Gradle
scoop install gradle
```

### 方法2：使用 Chocolatey
```powershell
choco install gradle
```

### 方法3：手动安装
1. 下载: https://gradle.org/releases/
2. 解压到 `C:\Gradle\`
3. 添加 `C:\Gradle\gradle-8.5\bin` 到 PATH 环境变量

## 生成 Wrapper 文件

安装完成后，在项目目录执行：

```powershell
cd andesk-launcher
gradle wrapper
```

这会生成：
- `gradlew` (Linux/Mac)
- `gradlew.bat` (Windows)
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
