package com.vincenthuto.mnagnosis.mixin.core;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityPhysics;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravitySourceMode;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGravityShiftMixin {

    @Unique
    private boolean mnagnosis$remapVanillaTravel;
    @Unique
    private double mnagnosis$capturedHorizontalDrag = 1.0D;
    @Unique
    private double mnagnosis$capturedVerticalDrag = 1.0D;

    @Shadow
    protected abstract float getJumpPower();

    @Inject(method = "travel", at = @At("HEAD"))
    private void mnagnosis$captureVanillaTravelBranch(
            Vec3 travelVector,
            CallbackInfo callback
    ) {
        LivingEntity self = (LivingEntity) (Object) this;
        GravityDirection gravity = GravityShiftApi.direction(self);
        self.setDeltaMovement(mnagnosis$applySurfaceControlGrip(
                self,
                self.getDeltaMovement(),
                gravity,
                travelVector
        ));
        boolean inForgeFluid = self.isInFluidType(
                self.level().getFluidState(self.blockPosition())
        );
        boolean ordinaryDryTravel = !self.isInWater()
                && !self.isInLava()
                && !self.isInFluidType()
                && !inForgeFluid
                && !self.isFallFlying();
        BlockPos supportPos = BlockPos.containing(self.position().add(
                gravity.downVector().scale(0.500001D)
        ));
        boolean vanillaGravityApplied = self.isControlledByLocalInstance()
                && !self.isNoGravity()
                && !self.hasEffect(MobEffects.LEVITATION)
                && (!self.level().isClientSide
                || self.level().hasChunkAt(supportPos));
        mnagnosis$remapVanillaTravel =
                GravityPhysics.shouldRemapVanillaTravel(
                        gravity,
                        ordinaryDryTravel,
                        vanillaGravityApplied
                );
        if (!mnagnosis$remapVanillaTravel) {
            return;
        }

        boolean discardFriction = self.shouldDiscardFriction();
        float blockFriction = self.level().getBlockState(supportPos)
                .getFriction(self.level(), supportPos, self);
        mnagnosis$capturedHorizontalDrag =
                GravityPhysics.vanillaHorizontalDrag(
                        discardFriction,
                        self.onGround(),
                        blockFriction
                );
        mnagnosis$capturedVerticalDrag = discardFriction
                ? 1.0D : (double) 0.98F;
    }

    @Inject(method = "travel", at = @At("TAIL"))
    private void mnagnosis$applyDirectionalGravity(
            Vec3 travelVector,
            CallbackInfo callback
    ) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!mnagnosis$remapVanillaTravel) {
            return;
        }
        GravityDirection gravity = GravityShiftApi.direction(self);
        double gravityAcceleration = self.getAttributeValue(
                ForgeMod.ENTITY_GRAVITY.get()
        );
        Vec3 remapped = GravityPhysics.remapVanillaTravel(
                self.getDeltaMovement(),
                gravity,
                gravityAcceleration,
                mnagnosis$capturedHorizontalDrag,
                mnagnosis$capturedVerticalDrag
        );
        self.setDeltaMovement(mnagnosis$applySurfaceControlGrip(
                self, remapped, gravity, travelVector
        ));
    }

    @Unique
    private Vec3 mnagnosis$applySurfaceControlGrip(
            LivingEntity self,
            Vec3 velocity,
            GravityDirection gravity,
            Vec3 travelVector
    ) {
        var state = GravityShiftApi.state(self);
        if (state == null || state.mode() != GravitySourceMode.SURFACE) {
            return velocity;
        }
        float yaw = self.getYRot() * Mth.DEG_TO_RAD;
        float sine = Mth.sin(yaw);
        float cosine = Mth.cos(yaw);
        Vec3 intendedLocalMovement = new Vec3(
                travelVector.x * cosine - travelVector.z * sine,
                0.0D,
                travelVector.z * cosine + travelVector.x * sine
        );
        return GravityPhysics.applySurfaceControlGrip(
                velocity, gravity, intendedLocalMovement
        );
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void mnagnosis$jumpAlongLocalUp(CallbackInfo callback) {
        LivingEntity self = (LivingEntity) (Object) this;
        GravityDirection gravity = GravityShiftApi.direction(self);
        if (gravity == GravityDirection.DOWN) {
            return;
        }
        self.setDeltaMovement(GravityPhysics.jump(
                self.getDeltaMovement(), gravity, getJumpPower()
        ));
        self.hasImpulse = true;
        callback.cancel();
    }
}
