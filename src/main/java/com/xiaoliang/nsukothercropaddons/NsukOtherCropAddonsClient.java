package com.xiaoliang.nsukothercropaddons;

import com.nsukcropapi.client.CropApiConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * 客户端入口：把模组列表里的「配置」按钮接到 NSUK 的统一作物设置界面。
 *
 * <p>本模组没有独立配置项，来源开关与自动发现统一由 {@code nsukcropapi} 管理。</p>
 */
@Mod(value = NsukOtherCropAddons.MODID, dist = Dist.CLIENT)
public final class NsukOtherCropAddonsClient {

    public NsukOtherCropAddonsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new CropApiConfigScreen(parent));
    }
}
