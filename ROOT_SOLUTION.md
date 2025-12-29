# Root权限解决方案

## 问题描述
在受限容器环境中，AAPT2 daemon无法启动，导致Android项目无法构建APK。

## Root解决方案
当拥有Root权限时，可以解除容器的系统级限制来解决AAPT2 daemon问题。

### 1. 检查当前系统权限
```bash
# 检查是否拥有Root权限
sudo -l
# 或者
whoami
# 检查容器运行权限
ps aux | grep container
```

### 2. 容器权限配置
如果在Docker容器中运行，需要使用以下参数启动容器：
```bash
# 启动具有特权的容器
docker run -it --privileged \
  --pid=host \
  --ipc=host \
  --security-opt seccomp=unconfined \
  --cap-add=SYS_ADMIN \
  --cap-add=SYS_PTRACE \
  --cap-add=NET_ADMIN \
  --cap-add=MKNOD \
  --cap-add=SETUID \
  --cap-add=SETGID \
  -v /tmp:/tmp \
  -v /dev/shm:/dev/shm \
  your_container_image
```

### 3. 系统级配置修改
在拥有Root权限的情况下，可以修改系统配置：

#### 3.1 修改系统安全限制
```bash
# 临时禁用SELinux（如果启用）
sudo setenforce 0

# 或者修改SELinux配置
sudo sed -i 's/SELINUX=enforcing/SELINUX=permissive/g' /etc/selinux/config
```

#### 3.2 调整内核参数
```bash
# 增加系统进程限制
echo 'kernel.pid_max = 4194304' | sudo tee -a /etc/sysctl.conf

# 增加共享内存限制
echo 'kernel.shmmax = 134217728' | sudo tee -a /etc/sysctl.conf
echo 'kernel.shmall = 2097152' | sudo tee -a /etc/sysctl.conf

# 应用配置
sudo sysctl -p
```

#### 3.3 用户权限配置
```bash
# 将当前用户添加到docker组
sudo usermod -aG docker $USER

# 或者修改Gradle运行权限
sudo chmod 755 /path/to/gradle/wrapper/
```

### 4. Android SDK权限配置
```bash
# 确保Android SDK目录有正确权限
sudo chown -R $USER:$USER $ANDROID_HOME
sudo chmod -R 755 $ANDROID_HOME

# 确保Gradle缓存目录权限正确
sudo chown -R $USER:$USER ~/.gradle
sudo chmod -R 755 ~/.gradle
```

### 5. 文件系统挂载参数
```bash
# 如果需要，重新挂载文件系统以支持更多功能
sudo mount -o remount,exec,suid /tmp
sudo mount -o remount,exec,suid /dev/shm
```

### 6. 防火墙和网络配置
```bash
# 确保网络连接正常
sudo ufw allow out 443
sudo ufw allow out 80
```

### 7. 运行构建命令
在完成上述Root权限配置后，运行构建命令：
```bash
# 清理之前的构建
./gradlew clean --no-daemon

# 构建项目
./gradlew assembleDebug --no-daemon --console=plain
```

### 8. 使用systemd运行守护进程（可选）
```bash
# 创建一个服务文件来管理AAPT2
sudo tee /etc/systemd/system/aapt2.service << EOF
[Unit]
Description=AAPT2 Service
After=network.target

[Service]
Type=forking
User=$USER
ExecStart=/path/to/aapt2
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# 启用并启动服务
sudo systemctl daemon-reload
sudo systemctl enable aapt2
sudo systemctl start aapt2
```

## 安全注意事项
1. 使用Root权限时要格外小心
2. 仅在开发环境中使用特权模式
3. 及时撤销不必要的权限
4. 遵循最小权限原则

## 替代方案
如果无法获得Root权限，可以考虑：
1. 使用完整的Android开发环境
2. 在物理设备上进行开发
3. 使用云开发环境
4. 使用虚拟机而非容器

## 总结
虽然Root权限可以解除许多限制，但最稳妥的解决方案仍然是在标准Android开发环境中进行构建，以确保安全性和稳定性。
