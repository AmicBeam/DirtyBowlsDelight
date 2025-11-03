package com.dirtybowlsdelight.mixin;

import com.dirtybowlsdelight.DirtyBowls;
import com.dirtybowlsdelight.DirtyBowlHelper;
import com.dirtybowlsdelight.SoupConsumptionTracker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 处理soup类食物的物品使用流程
 * 检测soup类食物，设置状态标志，拦截finishUsingItem()返回值替换bowl
 */
@Mixin(LivingEntity.class)
public abstract class ItemUsageDebugMixin {

    /**
     * 拦截completeUsingItem方法开始，检测soup类食物并设置状态标志
     */
    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void onCompleteUsingItemHead(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        ItemStack useItem = livingEntity.getUseItem();

        // 检测是否是soup类食物（有bowl作为容器）
        // 包括BowlFoodItem和FarmersDelight的ConsumableItem
        boolean isSoupItem = useItem.getItem() instanceof net.minecraft.world.item.BowlFoodItem;
        if (!isSoupItem) {
            // 检查是否有bowl作为容器
            ItemStack container = useItem.getCraftingRemainingItem();
            if (!container.isEmpty() && container.is(net.minecraft.world.item.Items.BOWL)) {
                isSoupItem = true;
            }
        }
        
        if (isSoupItem) {
            SoupConsumptionTracker.setProcessingSoupConsumption(true);
            DirtyBowls.LOGGER.info("[ItemUsageDebugMixin] Detected soup consumption: {}", useItem);
        }
    }

    /**
     * 拦截completeUsingItem中对finishUsingItem的调用
     * 这是在正确时机拦截，确保在结果返回给玩家之前进行替换
     */
    @Redirect(method = "completeUsingItem",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack redirectFinishUsingItemCall(ItemStack itemStack, Level level, LivingEntity entity) {
        ItemStack result = itemStack.finishUsingItem(level, entity);

        // 只有在处理soup且结果是碗时才替换为脏碗（带NBT）
        if (SoupConsumptionTracker.isProcessingSoupConsumption() && result.is(net.minecraft.world.item.Items.BOWL)) {
            ItemStack dirtyBowlStack = DirtyBowlHelper.createDirtyBowlStack(result);
            if (dirtyBowlStack != null) {
                DirtyBowls.LOGGER.info("[ItemUsageDebugMixin] Replacing bowl with dirty bowl: {} -> {}", result, dirtyBowlStack);
                return dirtyBowlStack;
            } else {
                DirtyBowls.LOGGER.warn("[ItemUsageDebugMixin] Dirty bowl item not found, keeping regular bowl");
            }
        }

        return result;
    }

    /**
     * 在completeUsingItem结束时重置标志
     */
    @Inject(method = "completeUsingItem", at = @At("TAIL"))
    private void onCompleteUsingItemTail(CallbackInfo ci) {
        // 重置soup食用标志
        SoupConsumptionTracker.setProcessingSoupConsumption(false);
    }

}
