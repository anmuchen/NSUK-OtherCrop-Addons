package com.xiaoliang.nsukothercropaddons;

import com.nsukcropapi.api.CropRules;
import com.nsukcropapi.api.CropSpec;
import com.nsukcropapi.api.CropWorkContext;
import com.nsukcropapi.api.StageOutcome;
import com.nsukcropapi.nsuk.WorkContainers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * 茶风·纪事的西瓜藤：age 7 是一个完整西瓜，只有左键破坏才掉西瓜 + 西瓜种子，
 * 空手/剪刀右键都不产出。收割直接按原版破坏方块处理，然后补种。
 *
 * <p>这个作物与界面里的「原版 西瓜」重名，所以注册时只给已有农田盒继续用，
 * {@link #hiddenFromUi(CropSpec)} 让选择界面不再显示这一条。</p>
 */
public final class MelonCropWork extends ManualFarmlandCropWork {

    public static final MelonCropWork INSTANCE = new MelonCropWork();

    private MelonCropWork() {
        super(7);
    }

    /** 西瓜是普通耕地作物，水渠照常挖（只有生咖啡豆/茶树/死神椒/马勃菌不挖）。 */
    @Override
    public Boolean needsWater(CropWorkContext ctx, CropSpec spec) {
        return null;
    }

    @Override
    public boolean hiddenFromUi(CropSpec spec) {
        // 旧的「茶风·纪事：追叙 西瓜」只留给已有农田盒；新选的西瓜走原版那一行。
        return spec != null && "ts_watermelon".equals(spec.id());
    }

    @Override
    public StageOutcome applyHarvest(CropWorkContext ctx, CropSpec spec) {
        if (!Boolean.TRUE.equals(needsHarvest(ctx, spec))) {
            return StageOutcome.SKIPPED;
        }
        var level = ctx.level();
        var pos = ctx.cropPos();
        List<ItemStack> drops = Block.getDrops(
                level.getBlockState(pos), level, pos, level.getBlockEntity(pos));
        WorkContainers.deposit(level, ctx.chests(), drops, pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        // 收完补种；下方耕地还在，直接种下一茬。
        if (Boolean.TRUE.equals(needsPlant(ctx, spec))
                && WorkContainers.consume(level, ctx.chests(), spec.seed())) {
            level.setBlock(pos, CropRules.plantState(spec), 3);
        }
        return StageOutcome.PROCESSED;
    }
}
