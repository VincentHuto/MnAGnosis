package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import com.vincenthuto.mnagnosis.Config;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SuspensionEvents {

    private SuspensionEvents() {
    }

    @SubscribeEvent
    public static void damage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)
                || event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return;
        }
        ForceDamageSuspension.armed(player, SuspensionLawHandler.DAMAGE)
                .ifPresent(debt -> {
                    ForceDamageSuspension.DamageCapture capture =
                            ForceDamageSuspension.captureDamage(
                                    event.getAmount(),
                                    Config.MAXIMUM_SUSPENDED_DAMAGE_FRACTION
                                            .get().floatValue()
                            );
                    event.setAmount(capture.remaining());
                    ForceDamageSuspension.addDamage(player, debt, capture.captured());
                });
    }

    @SubscribeEvent
    public static void knockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        ForceDamageSuspension.armed(player, SuspensionLawHandler.FORCE)
                .ifPresent(debt -> {
                    float capturedStrength = event.getStrength() * 0.5F;
                    event.setStrength(event.getStrength() - capturedStrength);
                    Vec3 captured = new Vec3(
                            event.getRatioX() * capturedStrength,
                            0.0D,
                            event.getRatioZ() * capturedStrength
                    );
                    ForceDamageSuspension.addForce(player, debt, captured);
                });
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END
                && event.player instanceof ServerPlayer player) {
            EffectExpirationSuspension.tick(player);
        }
    }
}
