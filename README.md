# EasyTouch - Android 快捷悬浮窗工具

一个功能丰富的 Android 悬浮窗快捷工具，采用螺旋动画展开的卫星菜单设计，提供便捷的系统快捷操作。

## 功能特性

### 核心功能
- **悬浮球** - 可拖动的圆形悬浮按钮，支持自由定位
- **螺旋菜单** - 10个快捷按钮沿螺旋线展开，炫酷动画效果
- **无需Root** - 使用系统无障碍服务实现高级功能
- **Material Design** - 采用现代化的设计风格和渐变效果

### 快捷操作

#### 基础功能
1. **主页** - 一键返回桌面
2. **返回** - 模拟系统返回键（需无障碍服务）
3. **最近任务** - 打开最近任务界面（需无障碍服务）
4. **截图** - 截图功能提示
5. **音量** - 弹出音量调节对话框
6. **锁屏** - 锁屏功能（需设备管理器权限）

#### 扩展功能
7. **搜索** - 调用系统搜索
8. **图库** - 打开系统图库
9. **联系人** - 打开联系人应用
10. **拨号** - 打开拨号界面

## 截图预览

### 螺旋菜单展开效果
- 10个卫星按钮沿螺旋线从中心向外展开
- 每个按钮带有延迟动画，形成流畅的螺旋效果
- 主按钮旋转45度作为展开指示

### UI 特点
- 蓝色渐变主按钮
- 半透明白色卫星按钮
- 白色边框和精美图标
- 弹性动画效果

## 安装说明

### 方式一：直接安装 APK
1. 下载 `app-release.apk`
2. 在 Android 设备上安装
3. 授予悬浮窗权限
4. 启动悬浮窗服务

### 方式二：从源码编译
```bash
# 克隆项目
git clone https://github.com/yourusername/easytouch.git
cd easytouch

# 编译项目
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 使用说明

### 首次使用
1. 打开应用
2. 点击"启动悬浮窗"按钮
3. 在系统设置中授予悬浮窗权限
4. 悬浮球出现在屏幕上

### 基本操作
- **拖动**：按住悬浮球可拖动到任意位置
- **展开菜单**：点击悬浮球展开螺旋菜单
- **收起菜单**：再次点击主按钮收起菜单
- **执行功能**：点击任意卫星按钮执行对应操作

### 启用高级功能
部分功能需要启用无障碍服务：
1. 进入系统设置 → 无障碍功能
2. 找到 "EasyTouch"
3. 启用无障碍服务
4. 返回和最近任务功能即可使用

## 权限说明

| 权限 | 用途 | 是否必需 |
|------|------|---------|
| SYSTEM_ALERT_WINDOW | 显示悬浮窗 | ✅ 必需 |
| FOREGROUND_SERVICE | 保持悬浮窗服务运行 | ✅ 必需 |
| BROADCAST_CLOSE_SYSTEM_DIALOGS | 关闭系统对话框 | ✅ 必需 |
| POST_NOTIFICATIONS | 显示通知 | ✅ 必需 |
| 无障碍服务 | 执行返回、最近任务 | ⚠️ 可选 |

## 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **最低 SDK**: 24 (Android 7.0)
- **目标 SDK**: 36 (Android 14)
- **架构**: Service + WindowManager

## 项目结构

```
app/src/main/
├── java/com/holylong/easytouch/
│   ├── MainActivity.kt              # 主界面
│   ├── service/
│   │   ├── FloatingService.kt       # 悬浮窗服务
│   │   └── EasyAccessibilityService.kt  # 无障碍服务
│   └── ui/theme/                    # Compose 主题
├── res/
│   ├── layout/
│   │   ├── layout_satellite_menu.xml  # 卫星菜单布局
│   │   └── layout_volume_dialog.xml   # 音量对话框
│   ├── drawable/                    # 图标资源
│   └── xml/
│       └── accessibility_service_config.xml
└── AndroidManifest.xml
```

## 开发说明

### 螺旋菜单算法
```kotlin
// 螺旋线参数
val startAngle = 150.0              // 起始角度
val angleIncrement = 38.0           // 角度增量
val startRadius = 60.dpToPx()      // 起始半径
val radiusIncrement = 12.dpToPx()   // 半径增量

// 计算每个按钮位置
val angle = startAngle + index * angleIncrement
val radius = startRadius + index * radiusIncrement
```

### 添加新功能
1. 在 `layout_satellite_menu.xml` 添加新的卫星按钮
2. 创建对应图标资源
3. 在 `FloatingService.kt` 初始化按钮
4. 实现功能方法
5. 调整螺旋参数以适应新的按钮数量

## 已知问题

- 锁屏功能需要设备管理器权限，未实现
- 截图功能仅提示，需使用系统快捷键
- 部分设备可能需要手动授予无障碍权限

## 待实现功能

- [ ] 设备管理器锁屏
- [ ] 无障碍服务截图
- [ ] 自定义按钮顺序
- [ ] 主题颜色自定义
- [ ] 手势操作

## 版本历史

### v1.0.0 (当前版本)
- ✅ 基础悬浮窗功能
- ✅ 10个螺旋展开的快捷按钮
- ✅ 无障碍服务集成
- ✅ 音量调节对话框
- ✅ 搜索、图库、联系人、拨号快捷方式

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 致谢

- 项目灵感来源于 iOS 的 AssistiveTouch
- 使用了 Material Design 的设计理念
- 感谢所有贡献者的支持

## 联系方式

- 作者：HolyLong
- 项目地址：https://github.com/yourusername/easytouch
- 问题反馈：https://github.com/yourusername/easytouch/issues

---

如果这个项目对你有帮助，请给个 ⭐️ Star 支持一下！
