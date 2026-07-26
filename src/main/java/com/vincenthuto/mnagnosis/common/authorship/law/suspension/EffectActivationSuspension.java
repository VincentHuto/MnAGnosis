package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import com.mna.Registries;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.crafting.ModifiedSpellPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

public final class EffectActivationSuspension {

    private EffectActivationSuspension() {
    }

    public static void release(
            ServerPlayer player,
            SuspensionPayload payload,
            SuspensionScheduler.ReleaseReason reason
    ) {
        ResourceLocation componentId = ResourceLocation.tryParse(
                payload.consequence().getString("component")
        );
        SpellEffect component = componentId == null
                ? null : Registries.SpellEffect.get().getValue(componentId);
        if (component == null) {
            return;
        }
        ModifiedSpellPart<SpellEffect> part;
        try {
            part = ModifiedSpellPart.fromNBT(
                    payload.consequence().getCompound("modified_part"),
                    Registries.SpellEffect.get()
            );
        } catch (RuntimeException ignored) {
            part = new ModifiedSpellPart<>(component);
        }
        SpellTarget target = target(player, payload);
        component.ApplyEffect(
                new SpellSource(player, InteractionHand.MAIN_HAND),
                target,
                part,
                new SpellContext(player.serverLevel(), ISpellDefinition.EMPTY)
        );
    }

    private static SpellTarget target(ServerPlayer player, SuspensionPayload payload) {
        if (payload.consequence().hasUUID("target")) {
            Entity entity = player.serverLevel().getEntity(
                    payload.consequence().getUUID("target")
            );
            if (entity != null && !entity.isRemoved()) {
                return new SpellTarget(entity);
            }
        }
        if (payload.consequence().contains("block")) {
            BlockPos block = BlockPos.of(payload.consequence().getLong("block"));
            if (player.serverLevel().isLoaded(block)) {
                return new SpellTarget(block, Direction.UP);
            }
        }
        return new SpellTarget(player);
    }
}
