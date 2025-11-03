package com.dirtybowlsdelight.mixin;

import com.dirtybowlsdelight.DirtyBowls;
import com.dirtybowlsdelight.SoupConsumptionTracker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 这个mixin拦截Player的drop方法，当背包满时掉落bowl时替换为dirty bowl
 * 只在食用soup时替换，避免影响其他bowl掉落操作
 */
@Mixin(Player.class)
public abstract class PlayerDropMixin {

    /**
     * 拦截Player.drop()方法的参数，只有在食用soup时才替换bowl
     * 这个方法会在背包满时被FarmersDelight调用
     */
    @ModifyVariable(
        method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"),
        argsOnly = true
    )
    private ItemStack modifyDroppedItemStack(ItemStack stack) {
        // 检查是否正在处理soup食用（使用共享的状态追踪器）
        if (stack != null && stack.is(Items.BOWL) && SoupConsumptionTracker.isProcessingSoupConsumption()) {
            net.minecraft.world.item.Item dirtyBowl = ForgeRegistries.ITEMS.getValue(new ResourceLocation("artisanal", "dirty_bowl"));
            if (dirtyBowl != null && dirtyBowl != Items.AIR) {
                ItemStack dirtyBowlStack = new ItemStack(dirtyBowl, stack.getCount());
                DirtyBowls.LOGGER.info("[PlayerDropMixin] Replacing bowl with dirty bowl from soup consumption (inventory full): {} -> {}", stack, dirtyBowlStack);
                return dirtyBowlStack;
            } else {
                DirtyBowls.LOGGER.warn("[PlayerDropMixin] Dirty bowl item not found!");
            }
        }

        return stack;
    }

    /**
     * 拦截Player.drop()方法的另一个重载版本（带三个参数）
     */
    @ModifyVariable(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private ItemStack modifyDroppedItemStackThreeParams(ItemStack stack) {
        // 检查是否正在处理soup食用（使用共享的状态追踪器）
        if (stack != null && stack.is(Items.BOWL) && SoupConsumptionTracker.isProcessingSoupConsumption()) {
            net.minecraft.world.item.Item dirtyBowl = ForgeRegistries.ITEMS.getValue(new ResourceLocation("artisanal", "dirty_bowl"));
            if (dirtyBowl != null && dirtyBowl != Items.AIR) {
                ItemStack dirtyBowlStack = new ItemStack(dirtyBowl, stack.getCount());
                DirtyBowls.LOGGER.info("[PlayerDropMixin] Replacing bowl with dirty bowl from soup consumption (inventory full, 3 params): {} -> {}", stack, dirtyBowlStack);
                return dirtyBowlStack;
            } else {
                DirtyBowls.LOGGER.warn("[PlayerDropMixin] Dirty bowl item not found!");
            }
        }

        return stack;
    }
}

