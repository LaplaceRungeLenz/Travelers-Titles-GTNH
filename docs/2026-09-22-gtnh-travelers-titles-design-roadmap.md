# GTNH Traveler's Titles：调研、设计与实施路线图

日期：2026-09-22。状态：待用户审阅；尚未编写模组代码、修改游戏实例或创建 GitHub 仓库。

## 1. 目标与确认范围

为 GTNH 2.9.0-beta-3 / Minecraft 1.7.10 / Forge 10.13.4.1614 实现 Traveler's Titles 类似的 RPG 地区进入提示。保留高可配置性、资源包覆盖能力，并正确区分生物群系、普通维度、行星、卫星、轨道空间站和动态私人维度。

用户指定测试实例：`G:\PrismLauncher\instances\GT_New_Horizons_2.9.0-beta-3_Java_17-26`。最终在用户 GitHub 新建私人仓库，同步源码，不创建 Release。实施顺序为：本设计及路线图获批 → 编程 → 测试与修复 → 创建私人仓库并同步。

建议项目名称 `Traveler's Titles GTNH`，独立 modid `travelerstitlesgtnh`，仓库 `Travelers-Titles-GTNH`。仓库名若已存在，不覆盖旧仓库，采用未占用的新名称并报告。当前 GitHub CLI 登录账号为 `LaplaceRungeLenz`。

首版包含：自动地区标题、可选副标题、淡入淡出、独立样式、边界防抖、配置界面、客户端命令、资源包文字/图形/音效、GTNH 兼容适配、诊断导出和测试报告。Waystones 专属触发、自定义领地系统、服务器强制下发配置、现代字体引擎及结构探测不纳入首版。

## 2. 已核实的事实

### 上游功能与实现

检视了上游默认分支 `26.1.2`，提交 `fde32a1b386db9d04ceb6acb3b92ccbf05a5088b`。

- `TitleRenderManager` 在玩家更新时读取维度与群系，管理最近显示缓存、冷却、黑名单及声音优先级。
- `TitleRenderer` 负责字体缩放、定位、阴影和按 tick 推进的透明度动画。
- `ConfigModule` 将群系、维度、声音和 Waystones 设置分开。
- 上游支持资源包语言条目覆盖标题及颜色，支持替换进入地区的声音。
- 当前源码使用现代注册表、文本组件、GUI API 和 Mixin 注入，不能直接在 1.7.10 编译运行。
- 上游对未知维度使用 `???`，缺少语言条目的群系可能不显示；本项目需要更完整的运行时名称回退。
- 上游 LICENSE 标识为 LGPL-3.0；若复用源码或资源，将保留许可证及归属说明，逐项记录素材来源，不默认复制第三方美术包。

源码依据：

- [上游固定提交](https://github.com/YUNG-GANG/Travelers-Titles/tree/fde32a1b386db9d04ceb6acb3b92ccbf05a5088b)
- [标题管理器](https://github.com/YUNG-GANG/Travelers-Titles/blob/fde32a1b386db9d04ceb6acb3b92ccbf05a5088b/Common/src/main/java/com/yungnickyoung/minecraft/travelerstitles/render/TitleRenderManager.java)
- [标题渲染器](https://github.com/YUNG-GANG/Travelers-Titles/blob/fde32a1b386db9d04ceb6acb3b92ccbf05a5088b/Common/src/main/java/com/yungnickyoung/minecraft/travelerstitles/render/TitleRenderer.java)
- [作者功能与资源包说明](https://www.curseforge.com/minecraft/mc-mods/travelers-titles)

### 用户实例

以下来自本机文件和 JAR 接口检查，并非仅依据网上的默认配置。实例共有 246 个模组 JAR。

| 项目 | 实例中的版本或状态 | 设计影响 |
|---|---|---|
| Galacticraft | 3.4.33-GTNH | 通过天体 API 获取名称与身份 |
| GalaxySpace | 1.1.142-GTNH | 部分星球共享群系；不能由群系反推星球 |
| AmunRa | 0.8.14 | 普通天体之外还有母舰动态维度 |
| GregTech / BartWorks | 5.09.54.133 | 含 Ross128b / Ross128ba 的 GC WorldProvider |
| Biomes O' Plenty / RWG | 2.1.0.2308 / alpha-1.5.2 | 群系运行时识别及旧版名称适配 |
| Twilight Forest | 2.7.40 | 独立维度与多个群系 |
| Personal Space | 1.0.40 | 动态分配维度，不能把 Provider 类当唯一实例身份 |
| EndlessIDs | 1.7.4；extendBiome=true | 不可硬编码 256 个群系或用 byte 截断 ID |
| Angelica / Hodgepodge | 2.2.10 / 2.7.196 | HUD 渲染与现有修复兼容性实测 |
| lwjgl3ify / GTNHLib | 3.0.31 / 0.11.46 | 使用实际新 Java / LWJGL 环境回归 |
| Java | Prism 实例指定 Oracle 26.0.2 | 最终必须测试用户实际运行组合 |
| 语言与 GUI | zh_CN、GUI scale 4 | 中文、Unicode 字体和大 GUI 缩放为必测项 |
| 已启用资源包 | Modernity-GTNH-2026-09-07、chromatic-tooltips-icon、DarkReimagined_2.0.1 | 保留并验证实际资源包叠加效果 |

配置示例：GalaxySpace 有 41 个 `dimensionID` 条目，涵盖天体与轨道类型；这不是“41 个可登陆星球”的保证。AmunRa 六个配置维度为 90–95；Ross128b 为 64、Ross128ba 为 63；暮色森林为 7；Personal Space 从 180 开始分配。上述数字仅用于验证当前环境，不作为代码中识别地区的依据。

已通过实际 JAR 的 `javap` 核实：`IGalacticraftWorldProvider#getCelestialBody()`，`CelestialBody#getName/getUnlocalizedName/getLocalizedName/getDimensionID()`，以及 `GalaxyRegistry` 的天体查询接口可用。GalaxySpace、Ross128 与 AmunRa 的相关 Provider 都沿用 GC Provider 体系。母舰有独立名称、母天体和航行状态接口。

GC 普通空间站可共享天体对象；`GalaxyRegistry#getCelestialBodyFromDimensionID()` 只查注册天体列表，不能单独覆盖所有动态空间站。因此优先读取当前 Provider，空间站与母舰走专门分支。

参考：[Galacticraft 3.4.33-GTNH API](https://github.com/GTNewHorizons/Galacticraft/tree/3.4.33-GTNH/src/main/java/micdoodle8/mods/galacticraft/api)、[WorldProviderOrbit](https://github.com/GTNewHorizons/Galacticraft/blob/3.4.33-GTNH/src/main/java/micdoodle8/mods/galacticraft/core/dimension/WorldProviderOrbit.java)。GalaxySpace 旧 GitHub 地址本次返回 404，相关结论以用户实例的 JAR 和配置为依据。

## 3. 方案选择

| 路径 | 优点 | 代价 | 判断 |
|---|---|---|---|
| 基于 1.7.10 Forge 重写客户端内核，保留上游交互思路 | 能针对 GTNH 身份系统与资源加载设计；依赖少 | 需要实现旧版适配层 | 推荐 |
| 整体向下移植现代上游 | 文件结构接近上游 | 注册表、文本、配置、GUI 和 Mixin 均需大改 | 维护成本高 |
| 将地区与数值 ID 全写入脚本/配置 | 初期实现较快 | 换配置就可能失效，动态维度覆盖差 | 不满足兼容目标 |

推荐采用客户端模组：服务器无需安装，不改世界生成、群系注册或维度注册。基础实现依赖 Forge；GC/AmunRa 为可选适配，不把它们的 JAR 打包进成品。GTNHLib 是否用于配置便利功能，以必要性评估决定；不为此引入现代 YUNG's API。

使用 GTNH 官方 starter 和对应构建工具链，锁定版本。优先 Java 8 字节码及老版可用标准库，以降低运行要求；验收范围以目标实例为准，不把字节码兼容等同于已经测试所有 Java 版本。官方模板含发布工作流，必须移除发布路径并关闭 Maven/CurseForge/Modrinth 自动发布。[官方模板说明](https://github.com/GTNewHorizons/ExampleMod1.7.10)

## 4. 核心设计

### 4.1 数据流与职责

`客户端 tick → 位置快照 → 地区身份解析 → 规则/语言/资源解析 → 触发状态机 → HUD 渲染与本地音效`

- `LocationTracker`：检查当前世界、本地玩家、坐标及区块可用性；默认每 5 tick 采样一次。位置/世界未变化时尽量复用结果。
- `LocationResolver`：输出维度实例、稳定别名、天体、轨道上下文、群系及可选显示名称。不进行绘制。
- `RuleRepository` / `NameResolver`：解析规则、资源覆盖、翻译与回退，并保留命中的规则来源供诊断。
- `TitleController`：处理候选地区稳定时间、冷却、缓存、世界切换和维度/群系优先级。
- `HudRenderer`：文字与 PNG 渲染、尺寸约束、淡入淡出及 GL 状态恢复；渲染回调不读磁盘或扫描注册表。
- `ClientCommands` / 配置 GUI：预览、重载、开关、检查当前地区、导出注册表与实际命中的规则。

核心状态机与规则计算尽量采用不依赖 Minecraft 的 Java 类型，便于自动化测试。使用 Forge/FML 客户端事件及资源重载监听，首版不计划引入 Coremod/Mixin。

### 4.2 地区身份：机器键与显示名称分离

1. 维度运行时身份至少包含当前会话、实际维度 ID 与 Provider；世界重载/断线清空会话状态。不能用“文字相同”或“Provider 类相同”判断同一个地区。
2. 已知普通维度使用稳定语义别名；未知维度保留 Provider 类、Provider 返回名称与 ID 作为匹配条件。ID 可以用于用户定点覆盖，但不是内置默认识别基础。
3. 天体优先取当前 GC Provider 的 `CelestialBody`；用未本地化键、原始名称和提供者上下文建立匹配信息，保留大小写原值，避免把所有天体名强制小写造成冲突。注册表查询是校验和回退路径。
4. 空间站保留实例 ID 与所属轨道的区别。客户端有同步名称时采用其名称；数据不可得时显示“某天体轨道空间站”或 Provider 名称，不读取仅存在于服务器的世界数据。
5. AmunRa 母舰与固定星球分开。可选显示母舰名称、停靠天体/航行中状态；同一维度内停靠上下文改变可配置是否再次提示。未同步数据时延迟解析或回退，不能报空指针。
6. 群系通过旧版世界群系 API 读取实际 `BiomeGenBase`，不读取底层 byte 群系数组，不假定 ID 上限为 255。遍历运行时实际注册表，跳过空项。
7. 1.7.10 不具备现代统一的群系命名空间注册键。已知群系建立可审核别名表；通用回退保存完整类名、原始名称与 ID。未知群系的跨整合包稳定性不能一概保证，因此提供别名覆盖与冲突诊断。
8. 群系缓存键包含维度上下文；同一 Space 群系出现在不同星球时仍会有正确的天体标题。

名称优先级：显式用户规则指定文字/翻译键 → 本模组专属语言键 → 适配层给出的原模组本地化名称 → 群系原始名/Provider 名 → 带 ID 的可辨认占位名称。使用本模组语言键修改标题不应改变地图和其他 UI 的名称。

### 4.3 触发与视觉行为

- 默认使用 RPG 风格的屏幕上部居中提示，避免覆盖准星；坐标、锚点、字号和宽度全部可调。
- 换维度/星球时显示大标题；群系可作为较小副标题同时出现。维度提示播放期间不再单独叠加同一时刻的群系提示与声音。
- 普通群系变化需持续稳定一段时间才显示；建议初值 15 tick，冷却 80 tick，最近群系缓存 5 项，均可关闭或调整。
- 冷却内连续变化只保留最新候选，过期地区不排长队补播；边界短暂经过不应反复闪烁。
- 可配置首次进世界是否提示、重生是否提示、换维度是否清空群系缓存，以及单独屏蔽任意地区。
- “仅地表提示”作为选项，默认关闭。1.7.10 通常为二维群系，不能把高度变化解释为现代洞穴群系，也不能用天空可见性屏蔽全部太空/地下维度。
- 地区未就绪、世界为 null、区块未收到时等待，不强制生成或加载区块。坐标取整使用 floor，覆盖负坐标。
- 暂停时暂停动画；F1 隐藏 HUD 时隐藏提示，F3 和 GUI 打开时行为可配。隐藏期间不积累陈旧标题；恢复后最多重评估当前地区一次。
- 文字按实际字体宽度居中，对过长标题缩放/换行至上限；中文与 Unicode 字体使用游戏字体渲染器。标题音效通过客户端 UI 路径播放，避免无大气天体把提示音当世界环境音衰减。

### 4.4 配置与资源包合同

分为两类配置：

- `config/travelerstitlesgtnh/general.cfg`：玩家行为偏好、总开关、音量、HUD 策略、性能采样、调试；用 Forge 配置 GUI 调整常用项。
- `config/travelerstitlesgtnh/overrides.json`：高级地区规则、别名和显式样式覆盖。采用 schemaVersion，提供字段说明与可复制示例。

规则支持维度 ID、Provider 类、天体未本地化键、群系别名/类名/原始名称/ID、维度内群系以及 BiomeDictionary 类型组合；默认提供精确匹配和受限通配，不实现任意脚本执行。相同对象可按维度分别设置标题、颜色、图形与声音。

资源包约定：

```text
pack.mcmeta                         # 1.7.10 资源包格式
assets/travelerstitlesgtnh/lang/en_US.lang
assets/travelerstitlesgtnh/lang/zh_CN.lang
assets/travelerstitlesgtnh/titles/index.json
assets/travelerstitlesgtnh/titles/*.json
assets/travelerstitlesgtnh/textures/titles/*.png
assets/travelerstitlesgtnh/sounds.json
assets/travelerstitlesgtnh/sounds/*.ogg
```

- 文本模式：颜色、阴影、缩放、行距、位置、淡入/停留/淡出、装饰线/背景均可覆盖。
- 图形模式：资源包指定透明 PNG 标题或背景/图标，配置显示尺寸和比例；图片缺失回退文字。首版提供机制和演示素材，不预先为所有星球手绘整套美术。
- 多包加载通过资源管理器读取所有同路径 index，按原版包优先级合并，按规则 ID 覆盖；不假定能枚举任意 ZIP 目录。规则文件和纹理同样遵循资源管理器优先级。
- 合并次序为内置默认 → 启用资源包从低到高 → 玩家显式覆盖。未填写字段继承，列表字段整体替换；行为偏好不由美术包强制改写。
- 匹配规则先按显式 priority，再按条件具体程度，再按资源优先级，最终按规则 ID 确定顺序，保证结果可重复。诊断命令报告获胜规则及来源。
- 语言与资源重载后清除解析缓存并原子替换资源快照。F3+T、切换资源包/语言和本模组 reload 命令均有对应验证。
- 错误 JSON、非法颜色、负时长、过大尺寸和无效资源路径有验证；坏条目回退，日志限频，不能导致客户端循环报错或崩溃。

兼容边界：原版 1.7.10 的 `.lang`、字体贴图和声音资源可以沿用。现代 Traveler's Titles 资源包使用的 JSON 语言、字体 provider、自定义字形及现代注册 ID，不能承诺原包直接兼容。提供迁移说明：翻译转旧版 `.lang`，地区映射到本项目别名，图形标题改为本项目 PNG 规则；现代字体系统整体回移不属于首版。

## 5. 实施路线图与验收

| 阶段 | 实施内容 | 阶段验收 |
|---|---|---|
| M1 工程骨架 | 官方 starter、锁版本、独立包名/modid、客户端代理、许可证/归属、无发布 CI | 可构建 JAR；干净客户端加载；专用服务器误装不加载客户端类；产物不含依赖模组 JAR |
| M2 核心提示 | 位置采样、维度/群系变化、状态机、双层标题、淡入淡出、客户端音效 | 自动化覆盖边界抖动、冷却最终候选、重连、同名地区、维度优先级、零时长和负坐标 |
| M3 配置和资源包 | cfg+GUI、JSON schema、语言/规则/纹理/声音覆盖、预览/reload/inspect/dump 命令 | 多包优先级正确；错误输入回退；热重载立即生效；示例包可独立使用 |
| M4 GTNH 适配 | GC、GalaxySpace、AmunRa、Ross128、空间站、Personal Space、已安装群系与普通维度别名 | 导出完整运行时目录；天体身份不依赖硬编码 ID；未知地区有名称回退；缺少可选模组时正常加载 |
| M5 实例回归 | 在目标实例的隔离测试副本及新测试存档中测试，保留资源包组合；修复问题 | GUI scale 4/中文、Angelica、实际 Java 26、传送、星球、多包重载等有日志/截图/结果表 |
| M6 私有同步 | 完成源码/文档/示例包/测试报告，建立 Git 历史，新建私人仓库并推送 | 远端 HEAD 与本地一致，private=true，无 Release，无发布工作流 |

基础阶段即编写核心逻辑的有意义测试；渲染最终以游戏内观察验证，不能用编译成功替代实测。

### 测试矩阵

1. 群系：运行时枚举全部非空注册群系，检查身份冲突、名称回退、规则解析与 ID 范围；实际进入原版/BOP/RWG、暮色、Thaumcraft 群系，以及多群系星球代表地点。
2. 天体：运行时导出 GC 注册表，区分可进入的固定维度、仅星图展示对象和动态实例。在测试存档对已注册可进入天体逐项检查名称解析并安排进入验证；不强制创建不可进入天体的世界。
3. 必测代表：月球、火星、小行星带、金星、Io、Barnarda C、Ross128b/Ross128ba、AmunRa 天体、普通空间站、不同天体轨道空间站、母舰及私人维度。母舰/空间站须使用有效初始化实例，不能以裸传送失败判定本模组失败。
4. 普通维度：主世界、下界、末地、暮色森林，以及实例实际注册的深暗之域、外域/梦境等；以运行时目录决定最终清单，不凭模组安装名称推断可进入性。
5. 生命周期：首次进入、重生、快速往返、不同存档相同 ID、断线重连、资源重载期间提示、动态维度同 Provider 不同实例。
6. 资源：中文/英文、Unicode、长标题、PNG 比例、多包覆盖、缺图/缺音、坏 JSON、零动画时长、禁用音效，以及用户现有三包组合。
7. 性能：固定地点、步行、飞行和边界反复穿越，确认渲染期间没有资源 IO/注册表扫描，记录额外耗时/采样频率。无实测数据前不承诺具体 FPS 提升或“零开销”。
8. 客户端属性：最小 Forge 环境、无 GC 环境、完整 GTNH 环境；有可用测试服务器时验证服务器不装本模组的连接。若无法完成多人实测，报告该项未测。

测试报告逐项使用“通过 / 失败 / 未测 / 不适用”，注明版本和证据。注册表枚举测试与逐一进入世界测试分开统计。目标是全部实际注册地区都有解析结果；不把代表性游戏测试写成“全部世界均已实测”。

用户日常实例和已有存档保持原状。优先创建独立 Prism 测试实例，只复用所需模组、配置和资源包，新建测试世界。仓库不提交游戏 JAR、私人存档、账号文件、服务器列表或整个游戏日志。

## 6. 交付物与边界

- 可复现构建的源码和本地测试 JAR。
- 中英文基础标题/配置说明，已知地区别名与自定义规则示例。
- 可安装的 1.7.10 示例资源包，演示文字、图形及音效替换；素材来源清晰。
- 配置与资源包作者指南、运行时地区目录、兼容测试报告和明确的未测项。
- `LaplaceRungeLenz/Travelers-Titles-GTNH` 私人仓库（名称可用时），只同步本项目文件。CI 如启用，只编译和测试，权限以只读为基础；不上传公开附件，不推送发布 tag，不调用任何发布服务。

本次仅完成源码/API/本地配置调研。尚未构建或运行新模组，也未验证新的 HUD 效果。当前系统 Git 配置有解析错误，调研时通过进程级 `GIT_CONFIG_NOSYSTEM=1` 绕过，未修改系统设置；后续版本控制沿用受控的进程级处理。网络读取有短暂失败，已通过 GitHub API 与本机 JAR 交叉补足关键证据。

## 7. 审阅决定

建议批准“1.7.10 客户端内核重写 + 运行时身份解析 + GTNH 可选适配 + 分层配置/资源包”方案，以及 M1–M6 顺序。用户同意后按本路线图进入编程、测试及私人仓库同步；只有实质性改变范围或遇到必须由用户提供的信息才再次询问。
