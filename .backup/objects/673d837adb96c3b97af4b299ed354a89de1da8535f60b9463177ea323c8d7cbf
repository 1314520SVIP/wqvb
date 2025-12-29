# Text Adventure Game

这是一个Android文字冒险游戏项目，玩家可以通过输入命令在不同的房间之间移动，寻找宝藏。

## 项目特性

- 5个房间：起始房间、花园、图书馆、地牢、宝藏室
- 支持命令：北、南、东、西移动，查看、帮助、退出
- 基于文本的用户界面
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

## 构建说明

本项目使用GitHub Actions进行云端构建，可以解决本地环境构建APK的问题。

### 云端构建

项目包含预配置的GitHub Actions工作流，位于 `.github/workflows/android_build.yml`，可自动构建Debug和Release版本的APK。

## 技术栈

- Android SDK
- Java
- AndroidX库
- Gradle构建系统

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

## 解决方案文档

项目包含详细的解决方案文档：

- `AAPT2_CONTAINER_SOLUTION.md` - AAPT2容器环境问题解决方案
- `MOBILE_DEVICE_SOLUTION.md` - 手机设备解决方案
- `ROOT_SOLUTION.md` - Root权限解决方案
- `ALTERNATIVE_BUILD_METHODS.md` - 替代构建方案
- `BUILD_INSTRUCTIONS.md` - 构建说明

## GitHub Actions配置

- `github_actions/android_build.yml` - GitHub Actions构建配置
- `github_actions/README.md` - 云端构建说明

## 部署

使用GitHub Actions工作流自动构建APK，下载产物即可安装。