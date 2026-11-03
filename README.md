# NSUK：其它作物附属

把「田园调酿！」「茶风·纪事：追叙」「烘焙坊」三个模组的作物接入 **New: Sim-U-Kraft（simukraft）** 的农田盒系统，交由雇农自动铺土、播种、施骨粉、收割与补种。

- 模组 ID：`nsukothercropaddons`
- 版本：1.0.0
- 运行环境：Minecraft 1.21.1 / NeoForge 21.1.65+
- 必需前置：`NSUK 作物 API`（`nsukcropapi`）1.0.0+、`New: Sim-U-Kraft`（`simukraft`）1.0+
- 可选联动：`concoction`（田园调酿）、`teastory`（茶风·纪事）、`bakeries`（烘焙坊）
- 许可证：GNU General Public License v3.0

---

## 收录范围

| 来源模组 | 作物 |
|---|---|
| 田园调酿（`concoction`） | 卷心菜、玉米、棉花、薄荷、洋葱、马勃菌、辣椒、番茄、向日葵、幽灵死神椒 |
| 茶风·纪事（`teastory`） | 辣椒、白菜、茶树；西瓜藤 |
| 烘焙坊（`bakeries`） | 番茄、咖啡、芋头 |

需要泡水的作物（如水稻）不在收录范围内。

## 作业规则

雇农会把选定区域整体铺成湿润耕地——箱子里备好泥土即可，不再要求目标格原本就有泥土。水渠照常开挖：芋头、番茄等普通作物沿用原版水渠灌溉，耕地不会干枯退化。

以下四种作物不按常规处理：

- **马勃菌**：脚下只要是完整方块即可种植，不翻地、不挖水渠，连原本属于水渠的那一列也会播种。
- **茶树**：`age 8` 后停止生长，必须用剪刀右键采摘，由 `TeaCropWork` 接管。
- **西瓜藤**：`age 7` 时须剪刀右键收瓜，由 `MelonCropWork` 接管。
- **生咖啡豆**：不挖水渠，耕地由附属定期补湿。

## 关于茶风西瓜

茶风·纪事的西瓜藤以原版 `melon` 这个 id 注册，因此选择界面中只保留一个「西瓜」选项，种植与收割全部走茶风西瓜的逻辑。旧存档中已有的 `ts_watermelon` 农田盒仍可继续运行，但不再出现在选择界面。

## 构建

本工程不使用 Gradle，直接调用 `javac` 与 `jar`：

```powershell
./build.ps1
```

脚本会从版本目录读取 NeoForge 版本号，并依赖以下两个文件（需先构建 `nsukcropapi-dev`）：

- `nsukcropapi-dev/build/mc-mojmap-1.21.1.jar`
- `nsukcropapi-dev/build/libs/nsukcropapi-1.0.0.jar`

产物输出至 `build/libs/nsukothercropaddons-1.0.0.jar`。

## 目录结构

```
src/main/java/com/xiaoliang/nsukothercropaddons/
├── NsukOtherCropAddons.java          模组入口，注册特殊作物
├── NsukOtherCropAddonsClient.java    客户端入口
├── GroundCropWork.java               马勃菌作业（免翻地）
├── TeaCropWork.java                  茶树作业（剪刀采茶）
└── MelonCropWork.java                西瓜藤作业（剪刀收瓜）
src/main/resources/
├── META-INF/neoforge.mods.toml       模组元数据
├── assets/nsukothercropaddons/lang/  中英文语言文件
└── data/nsukothercropaddons/other_crops/  作物 JSON 定义
```

## 作者

XiaoLiang小亮
