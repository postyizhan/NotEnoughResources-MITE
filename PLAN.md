# NotEnoughResources for MITE (EMI 附属 mod) 实施计划

## 目标

参考 `NotEnoughResources/`（1.7.10 Forge + NEI）的功能，在 MITE 1.6.4 / FishModLoader 3.4.x 上重写一份，
作为 `retroEMI-MITE`（mod id `emi`）的附属 mod 发布：通过 EMI 的 `emi` entrypoint 注册若干 recipe category，
展示矿物高度分布曲线、生物掉落、植物掉落、宝箱战利品与附魔。

## 进度与验证边界（随实施更新）

**已完成**：阶段 1～5 全部。`./gradlew build` 产出 `NotEnoughResources-1.0.0.jar`，
含 `fml.mod.json`（`${id}` 等占位符已正确展开）、`neresources.accesswidener`、两个 `.lang`、
五条 recipe 与五个 category。
**未完成**：阶段 6 的客户端目视验证（step 19、20 的截图确认部分）。

**阶段 6 的现状**：本环境未运行 `./gradlew runClient`（会拉起真实客户端窗口）。
step 19 的数据部分已用 `./gradlew probe` 以另一种方式验证（见下），
但"EMI 侧栏出现 5 个新分类"与曲线/实体的实际渲染效果仍需在本地目视确认。

**新增的验证手段：`./gradlew probe`**（`neresources.probe.HeadlessProbe`）。
不启动客户端、不需要 GUI，直接在 loom 重映射后的 MITE 类上跑，用来验证那些"只能靠跑起来"的假设。
当前 36 项全过（含五条数据链的端到端产出）：
- MITE 的 `Block`/`Item` 静态初始化在普通线程上可用。
- **九张宝箱静态表全部可读**，含先前判为最高风险的 `WorldServer.bonusChestContent`。
  表项数与手数核对无误（地牢 41/23、废矿井 51、金字塔 12、要塞 46/4/7、铁匠铺 56、奖励箱 9）。
- **20 个 `new EntityXxx(null)` 全部构造成功**，且经验值反过来印证了倍率表
  （Blaze/Witch/IronGolem 20 = 5×4、Ghast 10 = 5×2、EarthElemental 15 = 5×3、Wither 50、MagmaCube 3、牲畜 0）。
- `MITEChestScraper` 端到端产出 9 张表；`bonusChest` 9 条表项合并为 8 个物品，
  证明重复项合并逻辑生效（两条 `hatchetFlint`，权重 3 与 5）。
- `MITEOreScraper` 端到端产出 23 条矿物分布，耗时 50ms，**数值与脱离 MC 的独立基准完全一致**
  （dirt 峰值 4.501%、coal 最佳 y=91、iron y=3、lapis y=23），即两套独立实现互相印证。
  矿脉大小是从真实 `BiomeDecorator` 读出的，说明 access widener 在运行期同样生效。
- `MITEMobData` 产出 22 个生物，经验值由实体自己给出（PigZombie 15 = 5×3、Wight 10 = 5×2 等），
  女巫掉落池的概率也对得上手算（stick 29% = 1−(16/18)³，其余 15.8% = 1−(17/18)³）。
- `MITEPlantData` 产出 8 种植物；附魔侧扫出 195 件可附魔物品、862 组物品/附魔组合。

**客户端实测（用户本机 `./gradlew runClient`）已确认**：
- **mod 被真实加载**：FML 的 16 个 mod 列表里有 `neresources 1.0.0`。
- **access widener 在运行期生效**：日志里依次出现 `[AW] Widened class:` `BiomeDecorator`、
  `WorldGenDungeons`、`StructureMineshaftPieces`、`ComponentScatteredFeatureDesertPyramid`、
  `ComponentStrongholdChestCorridor`、`ComponentStrongholdLibrary`、`ComponentStrongholdRoomCrossing`、
  `ComponentVillageHouse2`、`WorldServer` —— 正是本 mod 声明的宝箱表与矿物生成类。
- `fml.mod.json` 的三个 entrypoint 都能按加载器的方式加载并实例化
  （`probe` 里反射构造 + 转型校验，可排除类名拼错、非 public、缺无参构造、未实现接口）。

**仍未验证的**（`probe` 覆盖不到，只能靠跑客户端到主菜单）：
- EMI 是否真的**调用** `NERPlugin.register`。上面只证明了它可被实例化。
  判据：日志出现 `EMI plugin registration started.` / `finished.`。
- `DistributionGraphWidget` 与 `EntityRenderHelper` 的 GL11 状态恢复是否正确。
- **生物掉落表的转录准确性**。这是手抄 25 个 `dropFewItems` 的结果，属于转录风险而非逻辑风险：
  错了也能编译、能跑、不报错，只是数字错。矿物那部分有独立校验，这部分没有。

**已知的取舍**（不是 bug，是显示上的简化）：
- 史莱姆/岩浆怪的经验值随体型变化（`getSize() * 3`），页面上显示的是构造时随机得到的那个体型。
- 女巫的药水槽实际会再掷一次得到六种药水之一，页面用抗火药水代表这一组。
- 绵羊羊毛显示白色、铁傀儡花显示玫瑰，实际取决于个体的 `getFleeceColor()` / 掉落时的子类型。
- MITE 特有的变种（Longdead、BoneLord、Revenant、DireWolf、Hellhound、Ghoul、Shadow、
  元素生物、Copperspine、蜘蛛变种等）尚未单独登记；它们多数继承上述父类的掉落逻辑或不掉落。

**已验证的**（有证据，可复现）：
- `./gradlew compileJava` 通过（须用 JDK 17，系统默认的 Zulu 25 会让 Gradle 8.5 报
  `Unsupported class file major version 69`）。反证做过：故意插入类型错误后构建确实失败。
- `./gradlew validateAccessWidener` 通过 → `neresources.accesswidener` 的字段签名全部能对上 MITE 字节码。
- 矿物分布数学：脱离 Minecraft 单独编译运行（分布相关 6 个类不依赖 MC），
  **实测冷启动 50ms、热路径 0.7ms**，并对上了 step 19 的验收标准：
  煤峰值 y=91（递增）、铁区间 0–66 峰值 y=3、青金石三角中心 y=23、copper 双峰可见；
  总量与手算配平一致（dirt 236% ≈ 20 脉 × 30 块 / 256 列）。

**未验证的**（只读过字节码，尚未跑起来）：
- EMI entrypoint 是否真的为第三方 mod 触发。读过 `EmiAgnosFish.getPluginsAgnos()` 确认机制，
  但没见它实际为本 mod 触发过。若不触发，阶段 4 之后全是死代码。
- 九张宝箱静态表的 `<clinit>` 能否在 EMI 的 reload daemon 线程上安全执行。
  `WorldServer.bonusChestContent` 风险最高——读它会在 MITE 从未设想的时机/线程上初始化 `WorldServer`。
- `new EntityXxx(null)` 是否全部安全。`EntityArachnid` 有判空，但这只证明**部分**路径考虑过 null。
- `DrawableWidget` 里 GL11 立即模式的状态恢复。写错的表现是**别的** EMI 控件渲染错乱。
- 生物掉落表的转录准确性。这是手抄 25 个 `dropFewItems` 的结果，属于转录风险而非逻辑风险：
  错了也能编译、能跑、不报错，只是数字错，且无自动化手段可发现。矿物那部分有独立校验，这部分没有。

**实施顺序的调整**：PLAN 原本是瀑布式（阶段 3 全部采集 → 阶段 4 全部集成 → 阶段 6 才验证）。
实施中发现两件当初不知道的事：数据准确性的验证成本极低（脱离 MC 单独跑，50ms），
而运行时集成的验证成本无法压缩（只能靠跑起来）。把唯一昂贵、唯一不可替代的验证放在最后是反的，
因此改为：先打一根最小垂直切片（category + 占位 recipe）确认 entrypoint 触发，
再补 headless 探针（直接调用各 scraper，只看抛不抛异常，可验 `<clinit>` 链与 null-world 实体构造），
然后才继续阶段 3 的其余部分。

**实施中查出的 PLAN 事实错误**：
1. redstone 矿脉块数是 **5**，原文的矿脉块数表漏了 redstone。
2. 地狱 silverfish 走 2 参 `genMinable`，即 `deep=false`；且 ×8 适用 → 40 脉/区块。
3. 废矿井战利品表在 **`StructureMineshaftPieces.mineshaftChestContents`**，不在 `ComponentMineshaftCorridor`。
4. `BlockRedstoneOre extends Block`（非 `BlockOre`）→ 地狱红石**不**走 0–135 的 `BlockOre` 分支，走全列均匀。
5. 生成光照：hostile 是 `blv <= rand.nextInt(isUnderOpenSky() ? 8 : 5)`，
   即**地下低于 5、露天低于 8**，且中间是概率性的；不是单一阈值 8。
6. `EntityLivestock.isWell()` 不是"吃饱"，是 `min(freedom, food, water) >= 0.25`（饲养状态）。
7. 动物生成需 `getFullBlockLightValue > 8`（草方块上），即光照**大于 8**。

## 已确认的关键事实（调研结论）

**EMI 插件接入**
- `EmiAgnosFish.getPluginsAgnos()` 走 `FishModLoader.getEntrypointContainers("emi", EmiPlugin.class)`；
  只要在 `fml.mod.json` 的 `entrypoints.emi` 里声明插件类即可，无需 `Runnable`（走标准 Fabric EntrypointStorage，
  仅需公开无参构造 + 可转型为 `EmiPlugin`）。
- 只有在没有任何 id 为 `emi` 的 provider 时才会 fallback 手动塞 MITEPlugin/VanillaPlugin，
  所以本 mod 声明 entrypoint 不会顶掉 EMI 自带插件。
- 重载由 `PingS2CPacket` → `EmiReloadManager.reload()` 触发（进入世界后），**在独立 daemon 线程**上跑，
  期间 `Minecraft.getMinecraft().theWorld` 通常可用，但不能依赖它——注册阶段不要碰渲染状态。
- 分类翻译键：`EmiUtil.translateId("emi.category.", id)` → `emi.category.<domain>.<path>`，
  与 MITEPlugin 的 `new ResourceLocation("MITE", id)` 一致（对应 `emi.category.MITE.food`）。

**MITE 矿物生成（`BiomeDecorator` / `WorldGenMinable` 字节码）**
- 主世界 `generateOres()` 调用次数（`genMinable(count, gen[, deep])`，内部 `count` 每次循环仅 1/10 概率真正生成，
  故实际每区块矿脉数 ≈ count/10）：
  dirt 200、gravel 200、coal 50、copper 40(deep)、silver 10(deep)、gold 20(deep)、iron 60(deep)、
  mithril 10(deep)、silverfish 5(deep)、redstone 10、diamond 5、lapis 5。
- 地狱世界（underworld）：gravel 300、copper 40、silver 10、gold 20、iron 60、mithril 10、adamantite 5、
  redstone 10、diamond 5、lapis 5，且 `underworld_y_offset != 0` 时 silverfish 50。
- `genMinable` 在 `underworld_y_offset != 0` 且非 gravel 时 `count *= 8`，adamantite 再 `*= 2`。
- 矿脉块数（构造器 `numberOfBlocks`）：dirt/gravel 32、coal 16、copper/silver/iron 6、redstone 5、gold 4、
  mithril/adamantite/diamond/lapis/silverfish 3。
- 高度区间 `[getMinVeinHeight, getMaxVeinHeight]`（主世界）：
  dirt 32–128、gravel 24–128、coal 16–96、copper 0–128、silver 0–96、gold 0–48、iron 0–64、
  mithril 0–32、adamantite/silverfish 0–24、redstone 0–24、diamond 0–32、lapis 8–40。
  underworld 一律 0–255。
- `getRandomVeinHeight` 得到归一化因子 `f`，再 `y = min + (int)(f * (max-min+1))`。`f` 的分布形态：
  - **递增型**（`f = max(u1,u2)`，pdf ∝ 2f）：dirt、gravel、coal
  - **递减型**（`f = min(u1,u2)`，pdf ∝ 2(1-f)）：silver、gold、iron、mithril、adamantite、silverfish、redstone、diamond
  - **三角型**（`f = (u1+u2)/2`，中心 0.5）：lapis
  - **copper 混合**：1/2 概率 `f = 0.4 + 0.6·u`（均匀于 [0.4,1]），1/2 概率 `f = min(u1,u2)`
- `generate(...)` 里矿脉实际块数会随深度缩放（含 `Math.min(..., 4.0f)` 上限等），**未完全逆推**；
  绝对百分比按 `numberOfBlocks` 估算，与 NER 一样标注为近似值。

**MITE 宝箱战利品**：静态 `WeightedRandomChestContent[]` 字段，分散在
`WorldGenDungeons.field_111189_a` / `chest_contents_for_underworld`、`StructureMineshaftPieces.mineshaftChestContents`、
`ComponentScatteredFeatureDesertPyramid.itemsToGenerateInTemple`、`ComponentStrongholdChestCorridor`、
`ComponentStrongholdLibrary`、`ComponentStrongholdRoomCrossing`、`ComponentVillageHouse2.villageBlacksmithChestContents`、
`WorldGeneratorBonusChest`。1.6.4 无 Forge `ChestGenHooks`，需反射读静态字段。
`WeightedRandomChestContent` 字段名为 `theItemId` / `min_quantity` / `max_quantity` + 继承的 `itemWeight`。

**生物掉落**：MITE 在 `EntityLiving.dropFewItems(boolean, DamageSource)` 里实现，需要真实 world/DamageSource，
无法静态提取 → 与 NER 一样**硬编码掉落表**（依据 MITE 字节码校对），并提供注册 API 供第三方补充。

**渲染**：`EmiDrawContext` 只有 `fill` / `drawTexture` / 文本，没有画线原语；LWJGL 2.9.0 的 `GL11` 立即模式可用
（NER 的 `RenderHelper` 就是这么画的），可在 `DrawableWidget` 里直接用 `GL11.GL_LINES`。
实体渲染可移植 NER `RenderHelper.renderEntity`（`RenderManager.instance.renderEntityWithPosYaw`）。

**本地化**：FML 的 `LanguageLoaderTrans` 同时支持 `assets/<domain>/lang/<locale>.lang` 与 `lang/<locale>.json`；
需在 client 初始化里 `ModResourceManager.addResourcePackDomain("neresources")`。

## 目录与模块划分

复用仓库根目录已有的 fish-example-mod 骨架（`src/`、`build.gradle`、`gradle.properties`），
把 `com.example` 替换为 `neresources`：

```
src/main/java/neresources/
  NEResources.java                  ModInitializer：日志、配置加载
  NEResourcesClient.java            ClientModInitializer：资源域注册、事件注册
  api/
    distribution/DistributionBase, DistributionSquare, DistributionTriangular,
                 DistributionCustom, DistributionMITEVein   ← 新增：按上面 pdf 形态生成 256 长数组
    utils/DistributionHelpers, ColorHelper, DropItem, PlantDrop, LightLevel,
          Conditional, Priority
    restrictions/DimensionRestriction, BiomeRestriction, Restriction, DimensionRegistry
    NEResourcesAPI.java             对外注册入口（registerOre/Mob/Plant/Dungeon）
  entry/
    OreMatchEntry, MobEntry, PlantEntry, DungeonEntry, EnchantmentEntry
  registry/
    OreRegistry, MobRegistry, PlantRegistry, DungeonRegistry, EnchantmentRegistry
  mite/
    MITEOreScraper.java             反射读 BiomeDecorator 的 WorldGenMinable + 内置 count 表 → 分布
    MITEMobData.java                硬编码 MITE 生物掉落表
    MITEChestScraper.java           反射读各 WeightedRandomChestContent[] 静态字段
    MITEPlantData.java              作物/草丛掉落
  emi/
    NERPlugin.java                  implements EmiPlugin，注册 category + recipe
    NERCategories.java              ORE / MOB / PLANT / DUNGEON / ENCHANTMENT
    recipe/OreEmiRecipe, MobEmiRecipe, PlantEmiRecipe, DungeonEmiRecipe, EnchantmentEmiRecipe
    widget/DistributionGraphWidget, EntityWidget, ChestWidget, MicroTextHelper
  config/NERConfig.java             读写 FishModLoader.CONFIG_DIR 下的 json
  util/RenderUtil, ReflectionUtil, TranslationUtil, LogHelper, MapKeys, SilkTouchUtil
src/main/resources/
  fml.mod.json                      entrypoints: main/client/emi
  neresources.accesswidener         按需放开 BiomeDecorator/WorldGenMinable 字段
  assets/neresources/lang/{en_US,zh_CN}.lang
  assets/neresources/textures/gui/*.png      （可选，优先用 EMI 原生 widget 而非整张 GUI 底图）
```

## 实施步骤

### 阶段 1：工程骨架
1. `gradle.properties`：`mod_id=neresources`、`mod_name=NotEnoughResources`、`archives_base_name=NotEnoughResources`、
   `maven_id=notenoughresources`、`mod_version=1.0.0`。
2. `build.gradle`：把 `accessWidenerPath` 与 `loom.mods` 的 `"modid"` 改为 `neresources`；
   加 `implementation "com.github.MinecraftIsTooEasy:EMI:1.1.27"`（EMI 作为强依赖），
   保留 `RustedIronCore`。processResources 增加 `id`/`name` 展开（已有）。
3. 删除 `com/example` 示例类与 `modid.mixins.json`（本 mod 初版不需要 mixin；
   如后续要 hook 世界生成再加回）。
4. `fml.mod.json`：`depends` 加 `"emi": ">=1.1.27"`；`entrypoints` 声明
   `main: neresources.NEResources`、`client: neresources.NEResourcesClient`、
   `emi: neresources.emi.NERPlugin`。

### 阶段 2：API 与分布数学
5. 移植 `DistributionHelpers`（square / triangular / ramp / rounded square / add / multiply / mean level），
   去掉 NBT（`writeToNBT` 依赖 IMC，MITE 无此机制，改为纯内存注册）。
6. 新增 `DistributionMITEVein`：给定 `minY/maxY/veinsPerChunk/veinSize/形态枚举`，
   解析式生成 256 长 `float[]`：
   - 对 `f` 的 pdf 按形态取 `2f` / `2(1-f)` / 三角 / copper 混合；
   - 把 `f` 的密度映射到 `y = min + f·(max-min+1)`，再乘 `veinsPerChunk·veinSize / 256`
     （每区块 256 列，得到"某 y 层某列出现该矿的概率"），
   - 最后对矿脉的垂直展开做一次宽度约 `veinSize^(1/3)` 的平滑（近似 vein 厚度）。
7. `OreMatchEntry`：沿用 NER 的合并逻辑（同 restriction 可合并、`getChances(extraRange)`、`bestY`、
   silk touch map、drops 列表），移除 dense ore/CoFH 相关。

### 阶段 3：MITE 数据采集
8. `MITEOreScraper`：
   - 内置 `count` 表（主世界/地狱两套，来自 `generateOres()` 字节码）与形态表；
   - 通过 access widener 或反射从 `new BiomeDecorator(BiomeGenBase.plains)` 的
     `dirtGen/gravelGen/coalGen/...` 字段读 `numberOfBlocks`（矿脉大小），失败则用内置默认值；
   - 高度区间同样内置（`getMinVeinHeight`/`getMaxVeinHeight` 依赖 World，不便调用）；
   - 产出 `RegisterOreMessage` 等价物 → `OreRegistry`，主世界与地狱分别带 `DimensionRestriction`。
   - 矿石 → 掉落物映射：`oreCoal→coal`、`oreDiamond→diamond`、`oreLapis→dye:4`、`oreRedstone→redstone`、
     `oreCopper/Silver/Gold/Iron/Mithril/Adamantium→` 对应矿块本身（MITE 需熔炼，掉落即矿石），
     `clay→clayBall×4`；按 MITE 实际 `dropBlockAsEntityItem` 行为逐项核对后填表。
9. `MITEChestScraper`：反射读上述静态字段，按 NER `DungeonEntry` 的算法折算
   `chance = (maxRolls+minRolls)/2 · weight/totalWeight`；`WorldGenDungeons` 主世界/地狱两张表分别登记。
10. `MITEMobData`：按 MITE 各 `EntityXxx.dropFewItems` 字节码逐个核对，硬编码
    `DropItem(item, min, max[, chance][, conditionals])`；实体实例用 `new EntityXxx(null)` 构造，
    渲染前再补 `worldObj`（同 NER 做法）。首版覆盖 MITE 全部原版生物。
11. `MITEPlantData`：作物（小麦/胡萝卜/土豆/西瓜/南瓜）与草丛种子掉落。

### 阶段 4：EMI 集成
12. `NERCategories`：5 个 `EmiRecipeCategory`，id 用 `new ResourceLocation("neresources", "ore"|"mob"|...)`，
    icon 用 `EmiStack.of(Block.oreIron)` / `EmiStack.of(Item.swordIron)` / `EmiStack.of(Block.tallGrass)` /
    `EmiStack.of(Block.chest)` / `EmiStack.of(Block.enchantmentTable)`。
13. `NERPlugin.register(EmiRegistry)`：
    - 触发阶段 3 的采集（放在 `register` 内，保证每次 reload 重算）；
    - `registry.addCategory(...)`；
    - 每个 entry 生成一条 recipe 并 `addRecipe`；
    - 矿物 recipe 的 `getInputs()` 留空、`getOutputs()` 放矿石与掉落物，使"查看用途/来源"能定位到；
      `supportsRecipeTree()` 返回 false（避免污染 BoM 树）。
14. Recipe 实现（各自 `addWidgets`）：
    - **OreEmiRecipe**：宽 ~144；左上 `GeneratedSlotWidget` 轮换显示矿石与掉落物；
      右侧 `DistributionGraphWidget`（GL11 折线 + 坐标轴箭头 + 最大百分比/minY/maxY 标注 +
      hover 显示 `Y: n (x.xx%)`）；底部 `Best Y` 文本；tooltip 追加 silk touch 与维度限制。
    - **MobEmiRecipe**：左侧 `EntityWidget` 渲染实体（随鼠标转头），右侧掉落物 slot 列表 + 数量文本，
      顶部名称/生成光照/经验；掉落数 > 一页时分页按钮。
    - **PlantEmiRecipe**：作物 slot + 各掉落物 slot + 概率百分比。
    - **DungeonEmiRecipe**：`ChestWidget`（可选，或直接用箱子 EmiStack）+ 战利品 slot 网格 + 概率 +
      "堆数 min–max" 文本。
    - **EnchantmentEmiRecipe**：按可附魔物品列出附魔名与等级区间（NER 的 usage handler 语义），
      挂在 `addRecipe` 上并靠 `getInputs()` 关联到工具/书。
15. `DistributionGraphWidget` 用 `DrawableWidget` + `tooltip(BiFunction)` 实现，
    GL11 调用前后正确 push/pop 与恢复颜色/纹理状态。

### 阶段 5：配置与本地化
16. `NERConfig`：`FishModLoader.CONFIG_DIR/neresources.json`，字段对应 NER `Settings`：
    `itemsPerColumn`、`cycleTimeSeconds`、`extraYRange`、`useDimensionNames`、`excludedEnchantments`、
    `oreVeinCountOverrides`（允许玩家/整合包按实际服务端配置校正）。
17. `en_US.lang` / `zh_CN.lang`：`emi.category.neresources.*` 5 条 + `ner.ore.bestY`、`ner.mob.biome`、
    `ner.mob.exp`、`ner.stacks`、`ner.dungeon.*`、silk touch/conditional 文案；
    `NEResourcesClient` 里 `ModResourceManager.addResourcePackDomain("neresources")`。

### 阶段 6：验证
18. `./gradlew build` 编译通过。
19. `./gradlew runClient` 手动验证：EMI 侧栏出现 5 个新分类；铁矿的曲线区间 0–64 且峰值偏下；
    煤矿峰值偏上（递增型）；青金石呈中心 24 附近的三角；地狱维度条目单独成条。
20. README 更新：说明百分比为估算值、数据来源（MITE 字节码）、如何用配置校正。

## 需要确认的取舍

1. **绝对百分比精度**：`generate()` 里矿脉块数随深度缩放的公式未完全逆推，Y 轴百分比是估算
   （NER 原版同样如此，README 已声明）。若要精确，后续可加一个"用真实世界跑蒙特卡洛采样"的可选模式
   （`getRandomVeinHeight` 是 public，可在进入世界后采样 10 万次得到精确形状）。
2. **生物掉落表维护成本**：硬编码需随 MITE 版本更新；已预留 `NEResourcesAPI` 让其它 mod 补充/覆盖。
3. **附魔视图**：EMI 没有 NEI 的 "usage handler" 概念，附魔信息用 recipe + `getInputs` 关联实现，
   交互略有差异。
