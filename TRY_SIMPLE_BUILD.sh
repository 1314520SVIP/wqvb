#!/bin/bash

# 简化构建脚本，尝试解决AAPT2 daemon问题

echo "开始尝试简化构建..."

# 设置环境变量
export ANDROID_HOME="/root/android-sdk/android-sdk-linux"
export JAVA_HOME="/usr/lib/jvm/java-21-openjdk-arm64"

# 显示当前环境信息
echo "当前目录: $(pwd)"
echo "Java版本: $(java -version 2>&1)"
echo "Gradle Wrapper存在: $(if [ -f ./gradlew ]; then echo '是'; else echo '否'; fi)"

# 清理之前的构建
echo "清理项目..."
./gradlew clean --no-daemon --console=plain || echo "清理失败，继续..."

# 尝试使用不同的参数构建
echo "尝试构建项目..."

# 选项1: 使用非常保守的参数
echo "尝试方式1: 最保守参数"
./gradlew assembleDebug \
  --no-daemon \
  --console=plain \
  --max-workers=1 \
  -Dorg.gradle.jvmargs="-Xmx1g -XX:MaxMetaspaceSize=256m -Djava.awt.headless=true" \
  --info

# 如果上面失败，尝试选项2
if [ $? -ne 0 ]; then
  echo "方式1失败，尝试方式2: 使用系统级参数"
  
  # 尝试设置系统级参数（如果可能）
  export GRADLE_OPTS="-Xmx1g -XX:MaxMetaspaceSize=256m -Djava.awt.headless=true"
  
  ./gradlew assembleDebug \
    --no-daemon \
    --console=plain \
    --max-workers=1 \
    --info
fi

# 如果仍然失败，尝试选项3
if [ $? -ne 0 ]; then
  echo "方式2失败，尝试方式3: 仅验证配置"
  ./gradlew check --no-daemon --console=plain --dry-run
fi

echo "构建尝试完成"
