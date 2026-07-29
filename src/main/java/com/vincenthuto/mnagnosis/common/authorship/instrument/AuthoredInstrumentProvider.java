package com.vincenthuto.mnagnosis.common.authorship.instrument;

import com.mna.api.spells.base.ISpellDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public interface AuthoredInstrumentProvider {

    ResourceLocation typeId();

    boolean supports(ItemStack stack);

    Optional<InstrumentSnapshot> snapshot(
            ServerPlayer player,
            ItemStack stack,
            ISpellDefinition spell
    );
}
