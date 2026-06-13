@echo off
echo ========================================
echo   AnDesk Launcher - Gradle Setup
echo ========================================
echo.

REM 检查是否已存在gradlew
if exist "gradlew.bat" (
    echo gradlew.bat already exists!
    goto :end
)

echo Downloading Gradle Wrapper...
echo.

REM 创建目录
if not exist "gradle\wrapper" mkdir "gradle\wrapper"

REM 下载 gradle-wrapper.jar
echo Downloading gradle-wrapper.jar...
powershell -Command "Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'"

REM 创建 gradle-wrapper.properties
echo Creating gradle-wrapper.properties...
(
echo distributionBase=GRADLE_USER_HOME
echo distributionPath=wrapper/dists
echo distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
echo zipStoreBase=GRADLE_USER_HOME
echo zipStorePath=wrapper/dists
) > gradle\wrapper\gradle-wrapper.properties

REM 创建 gradlew.bat
echo Creating gradlew.bat...
(
echo @rem
echo @rem Copyright 2015 the original author or authors.
echo @rem
echo @rem Licensed under the Apache License, Version 2.0 ^(the "License"^);
echo @rem you may not use this file except in compliance with the License.
echo @rem You may obtain a copy of the License at
echo @rem
echo @rem      https://www.apache.org/licenses/LICENSE-2.0
echo @rem
echo @rem Unless required by applicable law or agreed to in writing, software
echo @rem distributed under the License is distributed on an "AS IS" BASIS,
echo @rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
echo @rem See the License for the specific language governing permissions and
echo @rem limitations under the License.
echo @rem
echo.
echo @if "%%DEBUG%%"=="" @echo off
echo @rem ##########################################################################
echo @rem
echo @rem  Gradle startup script for Windows
echo @rem
echo @rem ##########################################################################
echo.
echo @rem Set local scope for the variables with windows NT shell
echo if "%%OS%%"=="Windows_NT" setlocal
echo.
echo set DIRNAME=%%~dp0
echo if "%%DIRNAME%%" == "" set DIRNAME=.
echo set APP_BASE_NAME=%%~n0
echo set APP_HOME=%%DIRNAME%%
echo.
echo @rem Resolve any "." and ".." in APP_HOME to make it shorter.
echo for %%%%i in ^("%%APP_HOME%%"^) do set APP_HOME=%%%%~fi
echo.
echo @rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
echo set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
echo.
echo @rem Find java.exe
echo if defined JAVA_HOME goto findJavaFromJavaHome
echo.
echo set JAVA_EXE=java.exe
echo %%JAVA_EXE%% -version ^>NUL 2^>^&1
echo if "%%ERRORLEVEL%%" == "0" goto execute
echo.
echo echo.
echo echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo echo.
echo echo Please set the JAVA_HOME variable in your environment to match the
echo echo location of your Java installation.
echo.
echo goto fail
echo.
echo :findJavaFromJavaHome
echo set JAVA_HOME=%%JAVA_HOME:"=%%
echo set JAVA_EXE=%%JAVA_HOME%%/bin/java.exe
echo.
echo if exist "%%JAVA_EXE%%" goto execute
echo.
echo echo.
echo echo ERROR: JAVA_HOME is set to an invalid directory: %%JAVA_HOME%%
echo echo.
echo echo Please set the JAVA_HOME variable in your environment to match the
echo echo location of your Java installation.
echo.
echo goto fail
echo.
echo :execute
echo @rem Setup the command line
echo.
echo set CLASSPATH=%%APP_HOME%%\gradle\wrapper\gradle-wrapper.jar
echo.
echo.
echo @rem Execute Gradle
echo "%%JAVA_EXE%%" %%DEFAULT_JVM_OPTS%% %%JAVA_OPTS%% %%GRADLE_OPTS%% "-Dorg.gradle.appname=%%APP_BASE_NAME%%" -classpath "%%CLASSPATH%%" org.gradle.wrapper.GradleWrapperMain %%*
echo.
echo :end
echo @rem End local scope for the variables with windows NT shell
echo if "%%OS%%"=="Windows_NT" endlocal
echo.
echo :omega
) > gradlew.bat

echo.
echo ========================================
echo   Setup Complete!
echo ========================================
echo.
echo Next steps:
echo   1. Open Android Studio
echo   2. File -^> Open -^> Select this folder
echo   3. Wait for project sync
echo.
echo Or run: gradlew.bat assembleDebug
echo.

:end
pause
