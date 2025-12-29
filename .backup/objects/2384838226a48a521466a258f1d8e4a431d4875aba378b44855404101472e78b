# GitHub Actions 构建说明

本项目使用GitHub Actions进行云端构建，可以解决本地环境无法构建APK的问题。

## 如何启用云端构建

1. 将项目推送到GitHub仓库
2. 在GitHub仓库中启用Actions
3. 将 `android_build.yml` 文件复制到 `.github/workflows/` 目录下

## 配置文件说明

本配置文件会：
- 自动检测代码提交并触发构建
- 设置Java 17和Android SDK环境
- 构建Debug和Release版本的APK
- 上传生成的APK文件作为构建产物

## 构建产物

构建完成后，你可以下载以下APK文件：
- Debug版本：`app-debug.apk`
- Release版本：`app-release.apk`

## 注意事项

- 确保项目包含所有必需的构建文件（build.gradle, gradle.properties等）
- 项目结构符合标准Android项目规范
- 如果需要签名Release版本，需要配置密钥库

## 替代方案

如果GitHub Actions不适用，还可以考虑：
- 使用GitLab CI
- 使用其他CI/CD服务
- 在本地标准Android Studio环境中构建
