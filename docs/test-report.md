# 测试报告 / Test report

测试日期：2026-09-22。目标：GTNH 2.9.0-beta-3 / Forge 10.13.4.1614 / Minecraft 1.7.10。
本报告区分自动测试、实际进入世界和静态检查，未将注册表枚举视为逐一实测。

## 环境与隔离

- 从用户提供的 Prism 实例复制模组、配置、资源包和启动组件，建立独立 `Travelers-Titles-GTNH-QA` 实例。
- 实际游戏运行 Java 26.0.2，中文，GUI scale 4，Angelica 与 EndlessIDs 开启。
- 保留 Modernity-GTNH、chromatic-tooltips-icon、DarkReimagined 三个现有资源包；另测本项目示例包。
- 创建独立创造模式测试世界；未复制或修改用户存档，未修改原实例。
- 探针仅由 `-Pqa` 编译，且要求游戏目录存在 `.ttgtnh-qa` 标记；正常产物不包含探针。

## 自动化与构建

`gradlew test spotlessApply build`：25 项 JUnit 测试通过，包含触发优先级、防抖、冷却、最新候选、
世界身份、零动画、规则继承/覆盖/错误输入、可选 API 缺失、BOP/HEE 别名、Unicode 和 `+` 变体。
图片回归测试验证重复读取非图片和截断 PNG 时在调用 OpenGL 前抛出可恢复的 IOException。
规则测试包含完整整数群系 ID 4096；该项是规则单元测试，不是扩展 ID 世界生成实测。

正常构建包含 Spotless、Checkstyle、重混淆；`scripts/verify-artifact.py` 验证 Java 8 class 版本、
唯一 modid、无 QA 类、无内嵌依赖 JAR。Java 8 字节码不表示完整 GTNH 能在 Java 8 运行。

## 游戏内验证

最终回归覆盖 14 次访问，覆盖 13 个不同维度：主世界、阿努比斯、小行星带、巴纳德C、火星、
罗斯128b、金星、木卫一、月球、罗斯128ba、暮色森林、下界、末地，再返回主世界。
探针使用注册表天体键选择维度，实际传送使用当次解析的 ID；未按整合包固定数字写死身份。
传送跳过火箭/传送门进度，只验证位置解析与 HUD。

运行时目录识别 216 个非空群系、83 个天体注册项、55 个静态注册维度；最高群系 ID 为 239。
天体数包含仅星图对象，不代表 83 个可进入世界。重复别名通过目录的 `biomeAliasCollisions` 暴露，
可用完整 ID、类名和维度条件细化规则。

资源包最终复测结果和目录见 `docs/qa/` 中的 JSON；截图为独立测试存档画面。
10 项游戏内资源检查全部通过：缺图回退、非图片回退、示例 PNG 解码、坏 JSON 警告、
本地覆盖重载、完整资源重载、重载后 PNG、音效事件注册、已加载图片损坏后的安全重载、图片恢复。
最终普通 JAR 与游戏内 QA JAR 的所有生产类和资源逐项字节一致；额外探针仅存在于 QA JAR。
声音验证包括事件注册与调用路径，未做扬声器录音或音量仪测量。

## 修复与复查

独立代码审查发现并修复：客户端 `chunkExists()` 恒真引起的未加载区块误判；
`Gui.drawRect()` 关闭混合导致图片/文字透明度失效；损坏图片首次显示以及后续资源重载崩溃。
还修复 BOP 替换原版群系、HEE 替换末地群系的别名，以及 `Extreme Hills+` 名称碰撞。

第二次启动时发现隔离实例残留旧 QA JAR，未把该轮作为新修复通过的证据；禁用旧 JAR 后重新验证。
后续发现从末地返回时通用测试传送未把玩家加入目标世界实体列表，导致区块发送停止。
修复仅限探针的末地传送路径，同时修复模组在未加载区块期间保留旧位置的状态。
最终探针等待目标维度身份与已加载区块一致，`verify-runtime-report.py` 再检查每个目标/实际 ID，
早期仅检查玩家 dimension 的返回记录不作为通过证据。

## 明确未测项与限制

- 未逐一进入全部群系/55 个维度；RWG、Thaumcraft 等以注册表覆盖和名称回退为主。
- 未创建真实动态空间站、母舰或 Personal Space 实例；它们的适配基于已安装 API 检查，仍需后续游戏回归。
- 未实测所有母舰迁移/同步时序、多玩家断线重连、专用服务器误装、最小 Forge 客户端。
- 未实测真实 ID >255 的群系世界；本实例注册目录尚未使用这些 ID。
- 未做不同分辨率/所有 GUI 缩放、英文切换的完整视觉矩阵，未做量化性能基准；不宣称零开销。
- 原版字体系统负责文本渲染；现代字体 provider/TTF/现代语言 JSON 需按指南迁移。

## 复现

在独立副本中安装一个且仅一个 `-Pqa` 构建，并创建 `.ttgtnh-qa` 空文件。
将 `examples/resourcepack` 复制为 `resourcepacks/TTGTNH-QA-resources` 并启用，即可运行图片损坏/恢复检查。
启动后探针生成 `ttgtnh-qa-results.json`、`config/travelerstitlesgtnh/location-catalog.json` 和截图。
复测后使用不带 `-Pqa` 的 `clean test build` 构建安装包，执行产物验证脚本。
使用 `python scripts/verify-runtime-report.py docs/qa/runtime-results.json` 检查已保存的实际访问和资源检查结果。
不要在日常实例安装探针，也不要提交整个游戏日志、账号文件或存档。

安装包 SHA-256：`5037ccc4cdfc27788b46254d41b70a41923517389c6e3b27a4f0c102a143283d`。
