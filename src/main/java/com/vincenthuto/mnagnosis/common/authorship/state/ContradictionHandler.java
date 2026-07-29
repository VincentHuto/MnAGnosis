package com.vincenthuto.mnagnosis.common.authorship.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public interface ContradictionHandler {
    ResourceLocation handlerId();

    default boolean canDeclareClosure(Contradiction debt) {
        return true;
    }

    default void onDebtCreated(ServerPlayer player, Contradiction debt) {
    }

    default void onClosed(ServerPlayer player, Contradiction debt) {
    }

    default void vent(ServerPlayer player, Contradiction debt) {
    }
}
