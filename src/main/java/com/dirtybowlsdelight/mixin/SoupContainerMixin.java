package com.dirtybowlsdelight.mixin;

import com.dirtybowlsdelight.DirtyBowls;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 这个mixin拦截BowlFoodItem的finishUsingItem方法，在Farmers Delight之后修改返回值
 */
@Mixin(BowlFoodItem.class)
public abstract class SoupContainerMixin {

    /**
     * 在finishUsingItem方法返回时修改结果
     * 这个会在Farmers Delight的SoupItemMixin之后执行
     */
    @Inject(
        method = "finishUsingItem",
        at = @At("RETURN"),
        cancellable = true
    )
    private void onFinishUsingItemReturn(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();

        DirtyBowls.LOGGER.info("[SoupContainerMixin] ===== BowlFoodItem.finishUsingItem RETURN =====");
        DirtyBowls.LOGGER.info("[SoupContainerMixin] Original result: {}", result);

        // 如果返回的是碗，则替换为脏碗
        if (result.is(Items.BOWL)) {
            Item dirtyBowl = ForgeRegistries.ITEMS.getValue(new ResourceLocation("artisanal", "dirty_bowl"));
            if (dirtyBowl != null && dirtyBowl != Items.AIR) {
                ItemStack dirtyBowlStack = new ItemStack(dirtyBowl, result.getCount());
                DirtyBowls.LOGGER.info("[SoupContainerMixin] Replacing bowl with dirty bowl: {} -> {}", result, dirtyBowlStack);
                cir.setReturnValue(dirtyBowlStack);
            } else {
                DirtyBowls.LOGGER.warn("[SoupContainerMixin] Dirty bowl item not found!");
            }
        } else {
            DirtyBowls.LOGGER.info("[SoupContainerMixin] Result is not a bowl, no replacement needed: {}", result);
        }
    }
}
