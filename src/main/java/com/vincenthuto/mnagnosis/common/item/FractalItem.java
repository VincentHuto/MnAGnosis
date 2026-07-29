package com.vincenthuto.mnagnosis.common.item;

import com.vincenthuto.mnagnosis.common.entity.item.FractalItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FractalItem extends Item implements FractalEntityItem {

    public FractalItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(
            Level level,
            Entity location,
            ItemStack stack
    ) {
        return FractalItemEntity.create(level, location, stack);
    }
}
