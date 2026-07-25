package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.entities.boss.DemonLord;
import com.mna.entities.rituals.DemonStone;
import com.vincenthuto.mnagnosis.common.progression.TruthEncounterService;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(value = DemonStone.class, remap = false, priority = 1000)
public abstract class DemonStoneMixin {

    @Shadow
    private boolean summonAsHostile;

    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
                    remap = true
            ),
            require = 1
    )
    private boolean mnagnosis$interceptDemonLordReveal(Level level, Entity expectedLeader) {
        DemonStone stone = (DemonStone) (Object) this;
        UUID casterId = stone.getCasterUUID();
        Player caster = casterId == null ? null : level.getPlayerByUUID(casterId);
        if (!this.summonAsHostile
                && expectedLeader instanceof DemonLord
                && caster != null
                && TruthEncounterService.interceptLeader(
                        caster, expectedLeader.position(), expectedLeader.getYRot()
                )) {
            return true;
        }
        return level.addFreshEntity(expectedLeader);
    }
}
