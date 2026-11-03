package com.xiaoliang.nsukothercropaddons;

import com.nsukcropapi.CropAddons;
import com.nsukcropapi.NsukCropApi;
import com.nsukcropapi.api.CropRegistry;
import com.nsukcropapi.api.CropSpec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * NSUK:其它作物附属 —— 把田园调酿 / 茶风·纪事 / 烘焙坊 的作物接进 NSUK 农田盒。
 *
 * <p>绝大多数作物都是标准 {@code CropBlock}，只需要 jar 里的 JSON 定义。三处例外单独注册：</p>
 * <ul>
 *   <li>马勃菌：脚下只要完整方块就能种，不翻地、不挖水渠 → {@link GroundCropWork}；</li>
 *   <li>茶树：age 8 停止生长，必须剪刀右键采茶 → {@link ShearHarvestWork}；</li>
 *   <li>西瓜藤：age 7 必须剪刀右键收瓜 → {@link ShearHarvestWork}。</li>
 * </ul>
 */
@Mod(NsukOtherCropAddons.MODID)
public class NsukOtherCropAddons {

    public static final String MODID = "nsukothercropaddons";
    public static final String GROUP_PREFIX = "source.nsukothercropaddons.";

    public NsukOtherCropAddons(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CropAddons.bootstrap(NsukOtherCropAddons.class, MODID, "other_crops");
            registerConcoctionPuffball();
            registerTeastorySpecialCrops();
        });
    }

    /** 马勃菌：下方是完整方块即可，免翻地、免水渠。 */
    private static void registerConcoctionPuffball() {
        if (!ModList.get().isLoaded("concoction")) {
            return;
        }
        Item spores = item("concoction:puffball_spores");
        Block crop = block("concoction:crop_puffball");
        if (spores == Items.AIR || crop == Blocks.AIR) {
            return;
        }
        CropRegistry.register(new CropSpec(
                "con_puffball",
                spores,
                spores,
                crop,
                crop.getDescriptionId(),
                "concoction",
                GROUP_PREFIX + "concoction",
                GroundCropWork.INSTANCE));
        NsukCropApi.LOGGER.info("[nsukothercropaddons] 马勃菌已注册（免翻地 / 免水渠）");
    }

    /** 茶树（剪刀采茶）/ 西瓜藤（左键破坏收瓜）。 */
    private static void registerTeastorySpecialCrops() {
        if (!ModList.get().isLoaded("teastory")) {
            return;
        }
        int registered = 0;

        Item teaSeeds = item("teastory:tea_seeds");
        Block teaPlant = block("teastory:tea_plant");
        if (teaSeeds != Items.AIR && teaPlant != Blocks.AIR) {
            CropRegistry.register(new CropSpec(
                    "ts_tea",
                    teaSeeds,
                    teaSeeds,
                    teaPlant,
                    teaPlant.getDescriptionId(),
                    "teastory",
                    GROUP_PREFIX + "teastory",
                    TeaCropWork.INSTANCE));
            registered++;
        }

        Block melonVine = block("teastory:watermelon_vine");
        if (melonVine != Blocks.AIR) {
            // 原版那一行「西瓜」的 id 就是 melon：用同一 id 注册茶风西瓜，
            // 选择界面里原版行让位，只留一个西瓜选项；种植/收割都按茶风西瓜走。
            CropRegistry.register(new CropSpec(
                    "melon",
                    Items.MELON_SEEDS,
                    Items.MELON_SEEDS,
                    melonVine,
                    Items.MELON.getDescriptionId(),
                    "minecraft",
                    "gui.nsukcropapi.select_crop.group_vanilla",
                    MelonCropWork.INSTANCE));
            // 旧存档里的 ts_watermelon 农田盒继续可用，但不再出现在选择界面。
            CropRegistry.register(new CropSpec(
                    "ts_watermelon",
                    Items.MELON_SEEDS,
                    Items.MELON_SEEDS,
                    melonVine,
                    Items.MELON.getDescriptionId(),
                    "teastory",
                    GROUP_PREFIX + "teastory",
                    MelonCropWork.INSTANCE));
            registered++;
        }

        NsukCropApi.LOGGER.info("[nsukothercropaddons] teastory 特殊作物注册 {} 种（茶树 / 西瓜）", registered);
    }

    private static Item item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
    }
}
