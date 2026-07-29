package com.vincenthuto.mnagnosis.common.item;

import com.vincenthuto.mnagnosis.common.entity.item.FractalItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class FractalBlockItem
        extends BlockItem
        implements FractalEntityItem {

    public FractalBlockItem(Block block, Properties properties) {
        super(block, properties);
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
