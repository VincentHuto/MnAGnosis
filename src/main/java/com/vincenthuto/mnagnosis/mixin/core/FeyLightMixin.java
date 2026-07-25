package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.entities.boss.FaerieQueen;
import com.mna.entities.rituals.FeyLight;
import com.vincenthuto.mnagnosis.common.progression.TruthEncounterService;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(value = FeyLight.class, remap = false, priority = 1000)
public abstract class FeyLightMixin {

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = true
            ),
            require = 1
    )
    private boolean mnagnosis$interceptFaerieQueenReveal(Level level, Entity expectedLeader) {
        FeyLight light = (FeyLight) (Object) this;
        UUID casterId = light.getCasterUUID();
        Player caster = casterId == null ? null : level.getPlayerByUUID(casterId);
        if (expectedLeader instanceof FaerieQueen
                && caster != null
                && TruthEncounterService.interceptLeader(
                        caster, expectedLeader.position(), expectedLeader.getYRot()
                )) {
            return true;
        }
        return level.addFreshEntity(expectedLeader);
    }
}
