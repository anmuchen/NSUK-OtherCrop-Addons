package com.xiaoliang.nsukothercropaddons;

import com.nsukcropapi.api.CropSpec;
import com.nsukcropapi.api.CropWorkContext;
import com.nsukcropapi.api.StageOutcome;
import com.nsukcropapi.crop.CropHarvest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 茶树：age 8 起必须剪刀右键采茶（age 11 给茶籽），左键破坏只掉木头/种子。
 * 因此收割阶段模拟「手持剪刀右键」，茶树本体留着继续长。
 */
public final class TeaCropWork extends ManualFarmlandCropWork {

    public static final TeaCropWork INSTANCE = new TeaCropWork();

    private TeaCropWork() {
        super(8);
    }

    @Override
    public StageOutcome applyHarvest(CropWorkContext ctx, CropSpec spec) {
        if (!Boolean.TRUE.equals(needsHarvest(ctx, spec))) {
            return StageOutcome.SKIPPED;
        }
        boolean harvested = CropHarvest.rightClickHarvest(
                ctx.level(), ctx.chests(), ctx.cropPos(), new ItemStack(Items.SHEARS));
        return harvested ? StageOutcome.PROCESSED : StageOutcome.SKIPPED;
    }
}
