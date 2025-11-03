package com.dirtybowlsdelight;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

/**
 * 工具类，用于创建带NBT的脏碗
 */
public class DirtyBowlHelper {
    
    /**
     * 创建带NBT的脏碗ItemStack
     * NBT格式：{bowl:{Count:1b,id:"minecraft:bowl"}}
     * 
     * @param count 数量
     * @return 带NBT的脏碗ItemStack，如果脏碗物品不存在则返回null
     */
    public static ItemStack createDirtyBowlStack(int count) {
        Item dirtyBowl = ForgeRegistries.ITEMS.getValue(new ResourceLocation("artisanal", "dirty_bowl"));
        if (dirtyBowl == null || dirtyBowl == Items.AIR) {
            return null;
        }
        
        ItemStack dirtyBowlStack = new ItemStack(dirtyBowl, count);
        
        // 设置NBT：{bowl:{Count:1b,id:"minecraft:bowl"}}
        CompoundTag nbt = dirtyBowlStack.getOrCreateTag();
        CompoundTag bowlTag = new CompoundTag();
        bowlTag.putByte("Count", (byte) 1);
        bowlTag.putString("id", "minecraft:bowl");
        nbt.put("bowl", bowlTag);
        
        return dirtyBowlStack;
    }
    
    /**
     * 创建带NBT的脏碗ItemStack（从现有ItemStack复制数量）
     * 
     * @param sourceStack 源ItemStack，用于获取数量
     * @return 带NBT的脏碗ItemStack，如果脏碗物品不存在则返回null
     */
    public static ItemStack createDirtyBowlStack(ItemStack sourceStack) {
        if (sourceStack == null || sourceStack.isEmpty()) {
            return null;
        }
        return createDirtyBowlStack(sourceStack.getCount());
    }
}

