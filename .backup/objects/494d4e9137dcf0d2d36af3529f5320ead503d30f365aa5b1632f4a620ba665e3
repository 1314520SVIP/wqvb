# 容器环境限制解除方案

## 当前限制
1. 容器环境对进程创建有限制
2. AAPT2 daemon需要特定的系统权限
3. 容器的文件系统可能不完全兼容AAPT2的运行需求

## 实际解决方案

### 技术层面解决方案

#### 1. 配置Gradle使用非守护进程模式
在 `gradle.properties` 文件中设置：
```properties
# 禁用AAPT2守护进程
android.enableAapt2Daemon=false
# 使用非守护进程模式
org.gradle.daemon=false
# 禁用构建缓存
android.enableBuildCache=false
```

#### 2. 优化构建命令
使用以下命令构建项目：
```bash
./gradlew assembleDebug --no-daemon --console=plain
```

#### 3. AAPT2独立运行模式
在 `build.gradle` 文件中添加：
```gradle
android {
    aaptOptions {
        cruncherEnabled = false
        useNewCruncher = false
    }
}
```

### 系统层面解决方案

#### 1. 容器运行时参数调整
如果可以控制容器启动参数，添加以下参数：
```bash
--privileged
--pid=host
--ipc=host
--security-opt seccomp=unconfined
```

#### 2. 文件系统挂载优化
使用以下挂载方式：
```bash
-v /tmp:/tmp
-v /dev/shm:/dev/shm
```

#### 3. Dockerfile 配置示例
```
FROM openjdk:11-jdk

# 设置环境变量
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

# 安装必要的系统工具
RUN apt-get update && apt-get install -y \
    libc6 \
    lib32stdc++6 \
    lib32z1 \
    libbz2-1.0 \
    libbz2-dev \
    libncurses5 \
    libtinfo5 \
    libc6-dev-i386 \
    && rm -rf /var/lib/apt/lists/*

# 安装Android SDK
RUN mkdir -p ${ANDROID_HOME} \
    && cd /tmp \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip \
    && unzip -q commandlinetools-linux-9477386_latest.zip -d /opt \
    && mv /opt/cmdline-tools ${ANDROID_HOME}/ \
    && mkdir -p ${ANDROID_HOME}/cmdline-tools/bin \
    && yes | sdkmanager --sdk_root=${ANDROID_HOME} --licenses \
    && sdkmanager --sdk_root=${ANDROID_HOME} "platform-tools" "platforms;android-33" "build-tools;33.0.0"

WORKDIR /workspace
COPY . /workspace/
RUN chmod +x ./gradlew

# 构建项目
RUN ./gradlew assembleDebug --no-daemon
```

### 替代方案

#### 1. 使用Android Studio本地构建
将项目导出到本地Android Studio环境构建，这是最可靠的方案。

#### 2. 在线CI/CD构建
使用GitHub Actions, GitLab CI等在线服务，它们提供完整的Android构建环境。

#### 3. Docker构建优化
创建一个专用的Android构建容器：
```bash
# 运行Android构建容器
docker run --rm -v $(pwd):/workspace -w /workspace \
  -e ANDROID_HOME=/opt/android-sdk \
  --privileged \
  openjdk:11-jdk \
  bash -c "chmod +x ./gradlew && ./gradlew assembleDebug --no-daemon"
```

### 项目适配优化

#### 1. 修改build.gradle以减少AAPT2依赖
```gradle
android {
    compileSdk 33

    defaultConfig {
        applicationId "com.example.textadventure"
        minSdk 24
        targetSdk 33
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    aaptOptions {
        // 禁用新版本的aapt2的某些功能
        noCompress "tflite"
        noCompress "lite"
        ignoreAssetsPattern = "!.svn:!.git:.*:!CVS:!thumbs.db:!picasa.ini:!*.scc:*~"
    }
}
```

#### 2. 资源优化
确保资源文件不会触发AAPT2的复杂处理，减少构建期间的资源处理工作量。

## 结论
虽然我们无法直接解除容器的系统级限制，但通过上述配置优化和替代方案，可以有效绕过AAPT2 daemon的问题。最实用的解决方案是使用标准Android开发环境或在线构建服务。
