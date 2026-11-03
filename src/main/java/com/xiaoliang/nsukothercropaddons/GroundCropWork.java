package com.xiaoliang.nsukothercropaddons;

import com.nsukcropapi.api.CropRules;
import com.nsukcropapi.api.CropSpec;
import com.nsukcropapi.api.CropWork;
import com.nsukcropapi.api.CropWorkContext;
import com.nsukcropapi.api.StageOutcome;
import com.nsukcropapi.crop.CropAge;
import com.nsukcropapi.nsuk.WorkContainers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * 「只要脚下是完整方块就能种」的作物作业策略（田园调酿的马勃菌）。
 *
 * <p>马勃菌的 {@code canSurvive} 只看下方方块是否 {@code isSolidRender}（或耕地 / 灵魂土），
 * 不需要耕地、不需要水渠，所以这里把 TILL / DIG_WATER 全部答成「不用做」，
 * 播种与收割都由本策略自己完成；扫描也关掉分组，避免水槽行逻辑掺进来。</p>
 */
public final class GroundCropWork implements CropWork {

    public static final GroundCropWork INSTANCE = new GroundCropWork();

    private GroundCropWork() {
    }

    @Override
    public Boolean needsWater(CropWorkContext ctx, CropSpec spec) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean needsTill(CropWorkContext ctx, CropSpec spec) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean usesGroupedFarmScan(CropWorkContext ctx, CropSpec spec) {
        return Boolean.FALSE;
    }

    /** 马勃菌不需要水渠，原来留作水渠的那一列也照常种。 */
    @Override
    public boolean allowWaterTroughCells(CropWorkContext ctx, CropSpec spec) {
        return true;
    }

    @Override
    public boolean canPlantAt(CropWorkContext ctx, BlockState soil, CropSpec spec) {
        if (ctx == null || ctx.level() == null || ctx.cropPos() == null) {
            return false;
        }
        return CropRules.plantState(spec).canSurvive(ctx.level(), ctx.cropPos());
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
        return canPlantAt(ctx, ctx.soil(), spec);
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
        return CropRules.isMatureFull(spec, state);
    }

    @Override
    public Boolean needsHarvest(CropWorkContext ctx, CropSpec spec) {
        if (ctx == null || ctx.level() == null || ctx.cropPos() == null) {
            return Boolean.FALSE;
        }
        BlockState state = ctx.level().getBlockState(ctx.cropPos());
        return CropRules.isOwnPlant(spec, state) && CropAge.isMaxAge(state);
    }

    @Override
    public StageOutcome applyHarvest(CropWorkContext ctx, CropSpec spec) {
        if (!Boolean.TRUE.equals(needsHarvest(ctx, spec))) {
            return StageOutcome.SKIPPED;
        }
        BlockState state = ctx.level().getBlockState(ctx.cropPos());
        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                state, ctx.level(), ctx.cropPos(), ctx.level().getBlockEntity(ctx.cropPos()));
        WorkContainers.deposit(ctx.level(), ctx.chests(), drops, ctx.cropPos());
        ctx.level().setBlock(ctx.cropPos(), Blocks.AIR.defaultBlockState(), 3);

        // 收完顺手补种；土壤没变，仍按「下方完整方块」判定。
        BlockState plantState = CropRules.plantState(spec);
        if (plantState.canSurvive(ctx.level(), ctx.cropPos())
                && WorkContainers.consume(ctx.level(), ctx.chests(), spec.seed())) {
            ctx.level().setBlock(ctx.cropPos(), plantState, 3);
        }
        return StageOutcome.PROCESSED;
    }
}
