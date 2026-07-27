package com.vincenthuto.mnagnosis.mixin.client;

import com.mna.items.sorcery.ItemSpell;
import com.mna.spells.crafting.SpellRecipe;
import com.mna.tools.render.WorldRenderUtils;
import com.vincenthuto.mnagnosis.common.particle.IneffableParticleEffects;
import com.vincenthuto.mnagnosis.common.particle.IneffableSpellVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldRenderUtils.class, remap = false)
public abstract class WorldRenderUtilsMixin {

    @Inject(
            method = "spawnFirstPersonParticlesForStack",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void mnagnosis$spawnIneffableFirstPersonParticles(
            ItemStack stack,
            HumanoidArm arm,
            CallbackInfo callback
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (minecraft.isPaused()
                || player == null
                || !(stack.getItem() instanceof ItemSpell spellItem)) {
            return;
        }

        SpellRecipe spell = SpellRecipe.fromNBT(
                spellItem.getSpellCompound(stack, player)
        );
        if (!IneffableSpellVisuals.containsIneffableComponent(spell)) {
            return;
        }

        Vec3 forward = player.getLookAngle().normalize();
        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        double handSide = arm == HumanoidArm.LEFT ? -0.40D : 0.40D;
        Vec3 origin = player.position()
                .add(0.0D, player.getEyeHeight() - 0.20D, 0.0D)
                .add(forward.scale(0.50D))
                .add(right.scale(handSide));
        long phase = player.tickCount * 2L + (arm == HumanoidArm.LEFT ? 1L : 0L);
        IneffableParticleEffects.handParticle(
                player.level(), origin, forward, phase
        );
        callback.cancel();
    }
}
