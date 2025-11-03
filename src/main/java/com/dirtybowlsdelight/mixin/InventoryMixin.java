package com.dirtybowlsdelight.mixin;

import com.dirtybowlsdelight.DirtyBowls;
import com.dirtybowlsdelight.SoupConsumptionTracker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 这个mixin拦截Inventory的add方法，当添加bowl时替换为dirty bowl
 * 只在食用soup时替换，避免影响其他bowl添加操作
 */
@Mixin(net.minecraft.world.entity.player.Inventory.class)
public abstract class InventoryMixin {

    /**
     * 拦截Inventory.add()方法的参数，只有在食用soup时才替换bowl
     */
    @ModifyVariable(
        method = "add(Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("HEAD"),
        argsOnly = true
    )
    private ItemStack modifyAddedItemStack(ItemStack stack) {
        // 检查是否正在处理soup食用（使用共享的状态追踪器）
        if (stack != null && stack.is(Items.BOWL) && SoupConsumptionTracker.isProcessingSoupConsumption()) {
            net.minecraft.world.item.Item dirtyBowl = ForgeRegistries.ITEMS.getValue(new ResourceLocation("artisanal", "dirty_bowl"));
            if (dirtyBowl != null && dirtyBowl != Items.AIR) {
                ItemStack dirtyBowlStack = new ItemStack(dirtyBowl, stack.getCount());
                DirtyBowls.LOGGER.info("[InventoryMixin] Replacing bowl with dirty bowl from soup consumption: {} -> {}", stack, dirtyBowlStack);
                return dirtyBowlStack;
            } else {
                DirtyBowls.LOGGER.warn("[InventoryMixin] Dirty bowl item not found!");
            }
        }

        return stack;
    }
}