package com.vincenthuto.mnagnosis.common.entity.item;

import com.vincenthuto.mnagnosis.common.item.FractalEntityItem;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class FractalItemEntity extends ItemEntity {

    public FractalItemEntity(
            EntityType<? extends FractalItemEntity> type,
            Level level
    ) {
        super(type, level);
    }

    public static FractalItemEntity create(
            Level level,
            Entity source,
            ItemStack stack
    ) {
        FractalItemEntity entity = new FractalItemEntity(
                EntityRegistry.FRACTAL_ITEM.get(),
                level
        );
        if (source instanceof ItemEntity) {
            CompoundTag state = new CompoundTag();
            source.saveWithoutId(state);
            entity.load(state);
        } else {
            entity.moveTo(
                    source.getX(),
                    source.getY(),
                    source.getZ(),
                    source.getYRot(),
                    source.getXRot()
            );
            entity.setDeltaMovement(source.getDeltaMovement());
        }
        ItemStack copiedStack = stack.copy();
        entity.setItem(copiedStack);
        entity.lifespan = copiedStack.getEntityLifespan(level);
        return entity;
    }

    public FractalItemEntityTraits traits() {
        ItemStack stack = getItem();
        if (stack.getItem() instanceof FractalEntityItem provider) {
            return provider.fractalEntityTraits(stack);
        }
        return FractalItemEntityTraits.STATIC;
    }
}
