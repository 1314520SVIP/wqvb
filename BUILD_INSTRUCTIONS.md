# Android Text Adventure Game - 构建说明

## 项目概述
这是一个使用Android Studio开发的文字冒险游戏，玩家可以通过输入命令在不同的房间之间移动，寻找宝藏。

## 项目结构
```
TextAdventure/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/textadventure/
│   │   │   ├── MainActivity.java
│   │   │   └── GameEngine.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   ├── styles.xml
│   │   │   │   └── colors.xml
│   │   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradlew
```

## 构建要求
- Android Studio Flamingo | 2022.2.1 或更高版本
- Android SDK 33 (编译SDK版本)
- Android SDK Build-Tools (最新版本)
- Android SDK Platform-Tools
- Java 8 或 11

## 构建步骤

### 使用 Android Studio (推荐)

1. **打开项目**
   - 启动 Android Studio
   - 选择 "Open an existing Android Studio project"
   - 浏览到项目根目录并选择

2. **同步 Gradle**
   - Android Studio 会自动检测并同步 Gradle 文件
   - 等待同步完成 (可能需要几分钟首次同步)

3. **检查依赖**
   - 确保所有依赖项都已成功下载
   - 查看底部的 Build 工具窗口确认无错误

4. **连接设备或启动模拟器**
   - 连接 Android 设备并启用开发者选项和 USB 调试
   - 或启动 Android Studio 内置的模拟器

5. **构建和运行**
   - 点击绿色的 "Run" 按钮 (▶️)
   - 或使用快捷键 Shift+F10 (Windows/Linux) 或 Control+R (macOS)
   - 选择目标设备并点击 "OK"

### 使用命令行

1. **打开终端/命令提示符**
   - 导航到项目根目录

2. **在 Linux/macOS 上:**
   ```bash
   ./gradlew build
   ./gradlew installDebug
   ```

3. **在 Windows 上:**
   ```cmd
   gradlew.bat build
   gradlew.bat installDebug
   ```

## 项目特性

### 游戏功能
- 5个房间：起始房间、花园、图书馆、地牢、宝藏室
- 支持北、南、东、西四个方向移动
- 支持命令：查看、帮助、退出
- 基于文本的用户界面

### 技术特点
- 使用 Material Design 组件
- 响应式布局设计
- 面向对象的游戏引擎架构
- 实时命令处理

## 游戏玩法

1. 游戏启动后，玩家位于起始房间
2. 可以输入以下命令：
   - `北` `南` `东` `西` - 移动到相邻房间
   - `查看` - 显示当前房间信息
   - `帮助` - 显示可用命令
   - `退出` - 退出游戏
3. 目标是找到宝藏室 (Treasure Room)

## 已知路径
- 起始房间 → 北 → 花园
- 起始房间 → 东 → 地牢
- 花园 → 南 → 起始房间
- 花园 → 东 → 图书馆
- 图书馆 → 西 → 花园
- 地牢 → 西 → 起始房间
- 地牢 → 北 → 宝藏室

## 部署说明

### 生成 APK 文件
在 Android Studio 中:
1. 点击 "Build" 菜单
2. 选择 "Generate Signed Bundle / APK"
3. 选择 "APK" 并点击 "Next"
4. 创建或选择密钥库
5. 填写密钥信息
6. 选择 "release" 构建类型
7. 点击 "Finish"

### 或使用命令行
```bash
./gradlew assembleRelease
```
APK 文件将生成在 `app/build/outputs/apk/release/` 目录中。

## 疑难解答

### 常见问题

1. **Gradle 同步失败**
   - 检查网络连接
   - 确保 Android SDK 路径正确配置
   - 尝试 "File" > "Invalidate Caches and Restart"

2. **依赖项下载失败**
   - 检查网络连接
   - 尝试使用代理
   - 确保 Gradle 版本兼容

3. **构建失败**
   - 检查 Java 版本兼容性
   - 确保 SDK 版本正确
   - 查看错误日志并修复

## 项目维护

### 开发工具版本
- Gradle: 7.4
- Android Gradle Plugin: 7.4.0
- 编译 SDK: 33
- 最小 SDK: 24

### 依赖项版本
- androidx.appcompat: 1.6.1
- com.google.android.material: 1.8.0
- androidx.constraintlayout: 2.1.4

## 扩展建议

1. 添加更多房间和谜题
2. 增加物品收集系统
3. 实现存档/读档功能
4. 添加背景音乐和音效
5. 优化界面设计

## 许可证

此项目为示例项目，可自由使用和修改。
