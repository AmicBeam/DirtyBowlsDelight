package com.dirtybowlsdelight.mixin;

import com.dirtybowlsdelight.DirtyBowls;
import com.dirtybowlsdelight.DirtyBowlHelper;
import com.dirtybowlsdelight.SoupConsumptionTracker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 这个mixin通过检测Farmers Delight的行为来精确替换bowl
 * 在BowlFoodItem.finishUsingItem的HEAD和RETURN处都进行拦截
 */
@Mixin(BowlFoodItem.class)
public abstract class FarmersDelightSoupMixin {


    /**
     * 在finishUsingItem开始时检测是否是Farmers Delight在处理
     * Farmers Delight会在HEAD处注入并设置cancelled = true
     */
    @Inject(
        method = "finishUsingItem",
        at = @At("HEAD"),
        cancellable = true
    )
    private void detectFarmersDelightProcessing(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        // 检查是否是BowlFoodItem（soup）
        if (stack.getItem() instanceof net.minecraft.world.item.BowlFoodItem) {
            SoupConsumptionTracker.setProcessingSoupConsumption(true);
        }
    }

    /**
     * 在finishUsingItem结束时进行最终的bowl替换
     * 这个会在Farmers Delight处理完成后执行
     */
    @Inject(
        method = "finishUsingItem",
        at = @At("RETURN"),
        cancellable = true
    )
    private void replaceBowlAtReturn(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        // 只在Farmers Delight处理的情况下进行替换
        if (SoupConsumptionTracker.isProcessingSoupConsumption()) {
            ItemStack result = cir.getReturnValue();

            // 如果返回的是碗，则替换为脏碗（带NBT）
            if (result != null && result.is(Items.BOWL)) {
                ItemStack dirtyBowlStack = DirtyBowlHelper.createDirtyBowlStack(result);
                if (dirtyBowlStack != null) {
                    DirtyBowls.LOGGER.info("[FarmersDelightSoupMixin] Replacing bowl with dirty bowl: {} -> {}", result, dirtyBowlStack);
                    cir.setReturnValue(dirtyBowlStack);
                } else {
                    DirtyBowls.LOGGER.warn("[FarmersDelightSoupMixin] Dirty bowl item not found!");
                }
            }
        }
    }
}
