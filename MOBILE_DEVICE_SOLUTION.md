# 手机设备解决方案

## 问题描述
在手机设备的容器环境中，AAPT2 daemon无法启动，导致Android项目无法构建APK。

## 手机设备环境特点
1. Android系统架构
2. 可能需要Root权限才能执行系统级修改
3. 使用Termux等终端模拟器运行构建工具
4. 存储权限和应用权限的特殊限制

## 手机设备解决方案

### 1. Termux环境配置
如果使用Termux运行构建工具：

```bash
# 更新Termux包
pkg update && pkg upgrade

# 安装必要的包
pkg install gradle openjdk-17

# 设置Java环境变量
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export ANDROID_HOME=$HOME/android-sdk
```

### 2. 存储权限处理
```bash
# 对于Android 11+，需要特殊权限访问外部存储
termux-setup-storage

# 创建符号链接来访问公共目录
ln -s /sdcard/Download ~/storage/downloads
```

### 3. Root权限获取（需要已Root的设备）
如果设备已Root，可以使用以下方法：

#### 3.1 使用Magisk或超级用户权限
```bash
# 通过su命令获取Root权限
su

# 或者在脚本中使用
su -c "mount -o remount,rw /system"
```

#### 3.2 修改系统限制（需要Root）
```bash
# 获取Root权限
su

# 增加进程限制（仅在Root环境下）
echo 65536 > /proc/sys/kernel/pid_max

# 修改SELinux策略（如果适用，仅在Root环境下）
setenforce 0
```

### 4. 替代构建方法
如果无法获取Root权限，可以使用以下方式：

#### 4.1 使用Termux特定配置
在Termux中创建`~/.gradle/gradle.properties`：
```properties
# 禁用守护进程
org.gradle.daemon=false
# 禁用并行构建
org.gradle.parallel=false
# 禁用构建缓存
org.gradle.caching=false
# 增加JVM内存
org.gradle.jvmargs=-Xmx2g
```

#### 4.2 使用轻量级构建选项
```bash
# 使用最小化构建配置
./gradlew assembleDebug --no-daemon --console=plain --quiet
```

### 5. Android特定环境配置

#### 5.1 安装Android SDK
```bash
# 创建SDK目录
mkdir -p ~/android-sdk/cmdline-tools

# 下载命令行工具
cd ~/android-sdk/cmdline-tools
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip commandlinetools-linux-9477386_latest.zip
mv cmdline-tools latest

# 设置环境变量
export ANDROID_HOME=$HOME/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```

#### 5.2 接受SDK许可证
```bash
# 接受所有SDK许可证
yes | sdkmanager --licenses
```

### 6. 项目配置优化
基于之前的配置优化，保持以下设置：

#### 6.1 gradle.properties优化
```properties
# 禁用AAPT2守护进程
android.enableAapt2Daemon=false
# 禁用Gradle守护进程
org.gradle.daemon=false
# 禁用构建缓存
org.gradle.caching=false
# 启用并行构建（如果内存充足）
org.gradle.parallel=true
# 启用构建缓存（如果支持）
org.gradle.configureondemand=true
```

#### 6.2 build.gradle优化
```gradle
android {
    // ... 其他配置保持不变
    
    aaptOptions {
        cruncherEnabled = false
        useNewCruncher = false
        noCompress "tflite"
        noCompress "lite"
    }
    
    // 禁用一些可能引起问题的功能
    buildFeatures {
        aidl false
        buildConfig false
        renderScript false
        shaders false
    }
}
```

### 7. 手机设备专用构建命令
```bash
# 在手机设备上运行构建
./gradlew clean assembleDebug \
  --no-daemon \
  --console=plain \
  --max-workers=1 \
  -Dorg.gradle.jvmargs="-Xmx2g -XX:+UseParallelGC"
```

### 8. 使用Shizuku进行系统级操作（无需Root）
如果设备已安装Shizuku，可以通过Shizuku API执行一些系统级操作：

1. 安装Shizuku应用
2. 授予权限
3. 使用Shizuku API执行需要更高权限的操作

### 9. 云端构建服务（推荐）
如果手机本地环境无法解决，建议使用云端构建服务：

#### 9.1 GitHub Actions
创建`.github/workflows/android.yml`：
```yaml
name: Android CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
        cache: gradle
    - name: Grant execute permission
      run: chmod +x gradlew
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### 10. 调试步骤
如果仍然遇到问题，可以尝试以下调试方法：

```bash
# 检查当前环境
echo "当前用户: $(whoami)"
echo "当前目录: $(pwd)"
echo "Java版本: $(java -version 2>&1 | head -1)"
echo "Gradle版本: $(gradle -version)"

# 检查存储空间
df -h

# 检查内存使用情况
free -h
```

### 11. 安全和性能建议
1. 在手机上运行构建时，确保电池电量充足
2. 关闭其他应用以释放内存
3. 使用稳定的网络连接下载依赖
4. 定期清理Gradle缓存释放存储空间

## 总结
在手机设备上构建Android项目有其特殊性，可能需要Root权限或使用云端服务。如果当前环境无法解决问题，建议使用标准的PC开发环境或云端CI/CD服务，以获得更稳定可靠的构建体验。
