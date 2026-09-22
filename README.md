[简体中文](README.md) | [English](README.en.md)

# Traveler's Titles GTNH

一个面向 Minecraft 1.7.10 Forge 的高可配置**客户端模组**，灵感来自 Traveler's Titles。
针对 GT New Horizons 2.9.0-beta-3 设计，提供 Galacticraft、GalaxySpace、AmunRa、
Ross128、动态空间站、私人空间及扩展群系 ID 的适配机制。动态空间站、母舰和多人连接尚未完成游戏内验证。

## 安装

将正常构建生成的 `travelerstitlesgtnh-0.1.0.jar` 放入客户端 `mods` 文件夹。
服务器无需安装；本模组不修改世界生成、维度或群系注册。
运行时仅必须安装 Forge，太空模组适配为可选功能。

## 功能

- 进入维度、行星或新群系时显示淡入淡出标题；维度提示优先。
- 群系边界防抖、最近访问缓存、冷却和最新候选合并。
- 中文、英文基础翻译，以及其他模组名称的运行时回退。
- 通过资源包自定义文字、颜色、PNG 标题、背景、图标、声音和动画。
- Forge 配置界面，以及客户端预览、重载和诊断命令。
- 读取实际运行时身份，不依赖整合包的固定维度 ID，不把群系 ID 限定为 0–255。

## 配置

打开**模组 → Traveler's Titles GTNH → 配置**，调整行为和音量设置。

- `config/travelerstitlesgtnh/general.cfg`：行为、冷却、屏幕提示的可见性和音量。
- `config/travelerstitlesgtnh/overrides.json`：各地区的名称、样式及显式本地覆盖。
- `/ttgtnh preview dimension` 或 `/ttgtnh preview biome`：关闭聊天框后预览当前位置的标题。
- `/ttgtnh reload`：重载配置和标题资源。
- `/ttgtnh toggle`：切换启用状态，并保存设置。
- `/ttgtnh inspect`：查看当前位置的标识和匹配规则来源。
- `/ttgtnh dump`：将运行时群系、天体目录导出到配置文件夹。

所有命令均在客户端执行，无需服务器管理员权限。
按 F3+T 可重载资源；行为设置始终由玩家控制。

参阅[资源包与配置指南](docs/resource-packs.md)，可安装的示例资源包位于 `examples/resourcepack`。
现代版本 Traveler's Titles 的字体与 JSON 资源包需要转换；本模组不包含现代字体引擎的移植。

## 构建与测试

```powershell
.\gradlew.bat test build
```

构建工具固定使用 Gradle 9.4.0 和 GTNH 构建约定插件 2.0.29，正常产物使用 Java 8 字节码。
Gradle 会准备所需的 Java 工具链；启动 Gradle 本身需要受支持的较新 JDK。
首次构建需要联网下载依赖。

`build/libs/*-dev.jar` 用于开发，`*-sources.jar` 包含源码，不带这两种后缀的 JAR 用于安装。
每个游戏实例只安装本模组的一个版本。

仓库不包含版本发布或自动发布工作流，用于私人源码同步。

## 许可证

本独立实现采用 MIT 许可证。许可证内容、致谢及工程模板来源分别见 [LICENSE](LICENSE) 和 [NOTICE.md](NOTICE.md)。
