package com.xiaoliang.nsukothercropaddons;

import com.nsukcropapi.api.CropRules;
import com.nsukcropapi.api.CropSpec;
import com.nsukcropapi.api.CropWork;
import com.nsukcropapi.api.CropWorkContext;
import com.nsukcropapi.api.StageOutcome;
import com.nsukcropapi.crop.CropAge;
import com.nsukcropapi.crop.FarmlandFillWork;
import com.nsukcropapi.nsuk.WorkContainers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 手工注册的非 JSON 作物：统一「自动铺耕地 + 不挖水渠 + 播种」这一套。
 *
 * <p>普通耕地方案由宿主 TILL / PLANT 完成，但宿主的 TILL 只认原本就是泥土/草方块的格子；
 * 农田盒搭在石头平台上时，箱子里有泥土也不会铺。这里改成一律用
 * {@link FarmlandFillWork} 把下方铺成湿润耕地，再去播种。</p>
 */
public abstract class ManualFarmlandCropWork implements CropWork {

    /** 成熟/可收割年龄；茶树 8、西瓜 7。 */
    private final int harvestAge;

    protected ManualFarmlandCropWork(int harvestAge) {
        this.harvestAge = harvestAge;
    }

    @Override
    public Boolean needsWater(CropWorkContext ctx, CropSpec spec) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean usesGroupedFarmScan(CropWorkContext ctx, CropSpec spec) {
        return Boolean.FALSE;
    }

    /**
     * 不挖水渠，原来留作水槽的那一行也照常翻地 / 播种 / 收割。
     *
     * <p>宿主把每 4 格 z 的第 4 行留作水槽，{@code needsPlantWork} / {@code needsHarvestWork}
     * 第一行就把这一行排除掉了。本策略既然接管了这两个阶段，就必须同时放开这里的限制，
     * 否则田里会固定空出一列（视觉上就是"缺一条"）。</p>
     */
    @Override
    public boolean allowWaterTroughCells(CropWorkContext ctx, CropSpec spec) {
        return true;
    }

    @Override
    public Boolean needsTill(CropWorkContext ctx, CropSpec spec) {
        if (ctx == null || ctx.level() == null || ctx.cropPos() == null) {
            return Boolean.FALSE;
        }
        return FarmlandFillWork.needsTill(ctx.level(), ctx.cropPos());
    }

    @Override
    public StageOutcome applyTill(CropWorkContext ctx, CropSpec spec) {
        if (!Boolean.TRUE.equals(needsTill(ctx, spec))) {
            return StageOutcome.SKIPPED;
        }
        return FarmlandFillWork.applyTill(ctx.level(), ctx.chests(), ctx.cropPos())
                ? StageOutcome.PROCESSED
                : StageOutcome.WAITING_MATERIAL;
    }

    @Override
    public Boolean needsPlant(CropWorkContext ctx, CropSpec spec) {
        if (ctx == null || ctx.level() == null || ctx.cropPos() == null) {
            return Boolean.FALSE;
        }
        BlockState cell = ctx.level().getBlockState(ctx.cropPos());
        if (!cell.isAir() && !cell.canBeReplaced()) {
            return Boolean.FALSE;
        }
        return ctx.level().getBlockState(ctx.cropPos().below()).is(Blocks.FARMLAND);
    }

    @Override
    public StageOutcome applyPlant(CropWorkContext ctx, CropSpec spec) {
        if (!Boolean.TRUE.equals(needsPlant(ctx, spec))) {
            return StageOutcome.SKIPPED;
        }
        if (!WorkContainers.consume(ctx.level(), ctx.chests(), spec.seed())) {
            return StageOutcome.WAITING_MATERIAL;
        }
        ctx.level().setBlock(ctx.cropPos(), CropRules.plantState(spec), 3);
        return StageOutcome.PROCESSED;
    }

    @Override
    public boolean isMature(CropWorkContext ctx, BlockState state, CropSpec spec) {
        return isOwnPlant(state, spec) && CropAge.ageOf(state) >= this.harvestAge;
    }

    @Override
    public boolean isOwnPlant(BlockState state, CropSpec spec) {
        return state != null && spec != null && state.is(spec.plantBlock());
    }

    @Override
    public Boolean needsHarvest(CropWorkContext ctx, CropSpec spec) {
        if (ctx == null || ctx.level() == null || ctx.cropPos() == null) {
            return Boolean.FALSE;
        }
        return isMature(ctx, ctx.level().getBlockState(ctx.cropPos()), spec);
    }

    @Override
    public Boolean needsBonemeal(CropWorkContext ctx, CropSpec spec) {
        if (ctx == null || ctx.level() == null || ctx.cropPos() == null) {
            return Boolean.FALSE;
        }
        return CropAge.ageOf(ctx.level().getBlockState(ctx.cropPos())) >= this.harvestAge
                ? Boolean.FALSE
                : null;
    }
}
