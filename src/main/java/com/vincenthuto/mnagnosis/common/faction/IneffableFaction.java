package com.vincenthuto.mnagnosis.common.faction;

import com.mna.api.faction.BaseFaction;
import com.mna.api.faction.IFaction;
import com.mna.api.sound.SFX;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class IneffableFaction extends BaseFaction {

    public IneffableFaction() {
        super(IneffableFactionRegistry.CASTING_RESOURCE_ID);
    }

    @Override
    public List<IFaction> getEnemyFactions() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getFactionGrimoire() {
        return ItemStack.EMPTY;
    }

    @Override
    public Item getTokenItem() {
        return null;
    }

    @Override
    public SoundEvent getRaidSound() {
        return SFX.Event.Faction.FACTION_RAID_COUNCIL;
    }

    @Nullable
    @Override
    public SoundEvent getHornSound() {
        return null;
    }

    @Override
    public Component getOcculusTaskPrompt(int tier) {
        return Component.translatable("mnagnosis.occulus.task.ineffable");
    }

    @Override
    public ResourceLocation getFactionIcon() {
        return IneffableFactionRegistry.FACTION_ICON;
    }

    @Override
    public int[] getManaweaveRGB() {
        return new int[]{255, 255, 255};
    }

    @Override
    public ChatFormatting getTornJournalPageFactionColor() {
        return ChatFormatting.WHITE;
    }
}
