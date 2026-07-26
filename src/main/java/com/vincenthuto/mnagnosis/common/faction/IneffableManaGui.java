package com.vincenthuto.mnagnosis.common.faction;

import com.mna.api.capabilities.resource.ICastingResourceGuiProvider;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class IneffableManaGui implements ICastingResourceGuiProvider {

    @Override
    public ResourceLocation getTexture() {
        return IneffableFactionRegistry.HUD_TEXTURE;
    }

    @Override
    public int getXPBarColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getBarColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getBarManaCostEstimateColor() {
        return 0xFF808080;
    }

    @Override
    public int getResourceNumericTextColor() {
        return 0xFF808080;
    }

    @Override
    public int getBadgeSize() {
        return 64;
    }

    @Override
    public int getFrameU() {
        return 0;
    }

    @Override
    public int getFrameV() {
        return 0;
    }

    @Override
    public int getFrameWidth() {
        return 153;
    }

    @Override
    public int getFrameHeight() {
        return 16;
    }

    @Override
    public int getFillWidth() {
        return 128;
    }

    @Override
    public int getFillStartY() {
        return 5;
    }

    @Override
    public int getFillHeight() {
        return 6;
    }

    @Override
    public ItemStack getBadgeItem() {
        return new ItemStack(ItemRegistry.INEFFABLE_HUD_BADGE.get());
    }

    @Override
    public int getLevelDisplayY() {
        return this.getFrameHeight() - 1;
    }
}
