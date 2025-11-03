package com.dirtybowlsdelight;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DirtyBowls.MOD_ID)
public class DirtyBowls {
    public static final String MOD_ID = "dirtybowlsdelight";
    public static final Logger LOGGER = LogManager.getLogger();

    public DirtyBowls() {
        LOGGER.info("DirtyBowlsDelight mod loaded!");

        // 在运行时检查ItemSoup的实际类名
        try {
            // 查找beetroot_soup物品来获取ItemSoup实例
            net.minecraft.world.item.Item beetrootSoup = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("minecraft", "beetroot_soup"));
            if (beetrootSoup != null) {
                LOGGER.info("[DirtyBowls] Beetroot soup item class: {}", beetrootSoup.getClass().getName());
                LOGGER.info("[DirtyBowls] Beetroot soup item simple name: {}", beetrootSoup.getClass().getSimpleName());
                LOGGER.info("[DirtyBowls] Is BowlFoodItem: {}", beetrootSoup instanceof net.minecraft.world.item.BowlFoodItem);
            } else {
                LOGGER.error("[DirtyBowls] Beetroot soup item not found!");
            }
        } catch (Exception e) {
            LOGGER.error("[DirtyBowls] Error checking items", e);
        }

        // Mixin会自动处理BowlFoodItem的返回值修改
        // 使用延迟初始化来确保在运行时能找到Artisanal的脏碗物品
    }
}
