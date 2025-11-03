package com.dirtybowlsdelight.mixin;

import com.dirtybowlsdelight.DirtyBowls;
import com.dirtybowlsdelight.DirtyBowlHelper;
import com.dirtybowlsdelight.SoupConsumptionTracker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 这个mixin拦截Inventory的add方法，当添加bowl时替换为dirty bowl
 * 只在食用soup时替换，避免影响其他bowl添加操作
 * 同时确保脏碗能够正确合并到现有堆叠（通过NBT匹配）
 */
@Mixin(net.minecraft.world.entity.player.Inventory.class)
public abstract class InventoryMixin {

    /**
     * 拦截Inventory.add()方法的参数，只有在食用soup时才替换bowl
     * 创建带NBT的脏碗：{bowl:{Count:1b,id:"minecraft:bowl"}}
     */
    @ModifyVariable(
        method = "add(Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("HEAD"),
        argsOnly = true
    )
    private ItemStack modifyAddedItemStack(ItemStack stack) {
        // 检查是否正在处理soup食用（使用共享的状态追踪器）
        if (stack != null && stack.is(Items.BOWL) && SoupConsumptionTracker.isProcessingSoupConsumption()) {
            ItemStack dirtyBowlStack = DirtyBowlHelper.createDirtyBowlStack(stack);
            if (dirtyBowlStack != null) {
                DirtyBowls.LOGGER.info("[InventoryMixin] Replacing bowl with dirty bowl from soup consumption: {} -> {}", stack, dirtyBowlStack);
                return dirtyBowlStack;
            } else {
                DirtyBowls.LOGGER.warn("[InventoryMixin] Dirty bowl item not found!");
            }
        }

        return stack;
    }

    /**
     * 在Inventory.add()方法开始时拦截，如果是脏碗，先手动查找现有堆叠并合并
     * 确保脏碗能够正确合并到现有堆叠（使用ItemStack.isSameItemSameTags检查NBT匹配）
     */
    @Inject(
        method = "add(Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onAddHead(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // 如果是脏碗且正在处理soup食用，先手动合并
        if (stack != null && SoupConsumptionTracker.isProcessingSoupConsumption()) {
            net.minecraft.world.item.Item dirtyBowl = ForgeRegistries.ITEMS.getValue(new ResourceLocation("artisanal", "dirty_bowl"));
            if (dirtyBowl != null && stack.getItem() == dirtyBowl) {
                net.minecraft.world.entity.player.Inventory inventory = (net.minecraft.world.entity.player.Inventory) (Object) this;
                
                // 查找现有脏碗堆叠并合并（使用ItemStack.isSameItemSameTags确保NBT匹配）
                for (int i = 0; i < inventory.items.size(); i++) {
                    ItemStack existingStack = inventory.items.get(i);
                    // 使用isSameItemSameTags确保NBT匹配，只有NBT相同的才能合并
                    if (ItemStack.isSameItemSameTags(existingStack, stack) && existingStack.getCount() < existingStack.getMaxStackSize()) {
                        int spaceLeft = existingStack.getMaxStackSize() - existingStack.getCount();
                        int toAdd = Math.min(spaceLeft, stack.getCount());
                        
                        if (toAdd > 0) {
                            existingStack.grow(toAdd);
                            stack.shrink(toAdd);
                            
                            DirtyBowls.LOGGER.info("[InventoryMixin] Manually merged {} dirty bowls into existing stack (NBT matched)", toAdd);
                            
                            if (stack.isEmpty()) {
                                cir.setReturnValue(true);
                                return;
                            }
                        }
                    }
                }
                
                // 如果还有剩余，让原方法处理（添加到空槽位或返回false）
                // 如果stack为空，说明已经完全合并，返回true
                if (stack.isEmpty()) {
                    cir.setReturnValue(true);
                }
                // 否则让原方法继续处理
            }
        }
    }

    /**
     * 在Inventory.add()方法内部拦截，查找现有脏碗堆叠并合并
     * 如果add()返回false（添加失败），尝试手动合并到现有堆叠
     */
    @Inject(
        method = "add(Lnet/minecraft/world/item/ItemStack;)Z",
        at = @At("TAIL"),
        cancellable = true
    )
    private void onAddTail(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // 如果添加失败，且是脏碗，尝试手动合并
        if (!cir.getReturnValue() && stack != null && SoupConsumptionTracker.isProcessingSoupConsumption()) {
            net.minecraft.world.item.Item dirtyBowl = ForgeRegistries.ITEMS.getValue(new ResourceLocation("artisanal", "dirty_bowl"));
            if (dirtyBowl != null && stack.getItem() == dirtyBowl) {
                net.minecraft.world.entity.player.Inventory inventory = (net.minecraft.world.entity.player.Inventory) (Object) this;
                
                // 查找现有脏碗堆叠（使用ItemStack.isSameItemSameTags确保NBT匹配）
                for (int i = 0; i < inventory.items.size(); i++) {
                    ItemStack existingStack = inventory.items.get(i);
                    if (ItemStack.isSameItemSameTags(existingStack, stack) && existingStack.getCount() < existingStack.getMaxStackSize()) {
                        int spaceLeft = existingStack.getMaxStackSize() - existingStack.getCount();
                        int toAdd = Math.min(spaceLeft, stack.getCount());
                        
                        if (toAdd > 0) {
                            existingStack.grow(toAdd);
                            stack.shrink(toAdd);
                            
                            DirtyBowls.LOGGER.info("[InventoryMixin] Manually merged {} dirty bowls into existing stack (NBT matched)", toAdd);
                            
                            if (stack.isEmpty()) {
                                cir.setReturnValue(true);
                                return;
                            }
                        }
                    }
                }
                
                // 如果还有剩余，尝试添加到空槽位
                if (!stack.isEmpty()) {
                    for (int i = 0; i < inventory.items.size(); i++) {
                        if (inventory.items.get(i).isEmpty()) {
                            inventory.items.set(i, stack.copy());
                            stack.setCount(0);
                            cir.setReturnValue(true);
                            DirtyBowls.LOGGER.info("[InventoryMixin] Manually added dirty bowl to empty slot");
                            return;
                        }
                    }
                }
            }
        }
    }
}