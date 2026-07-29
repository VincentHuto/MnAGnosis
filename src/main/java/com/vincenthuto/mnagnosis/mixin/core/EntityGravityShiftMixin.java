package com.vincenthuto.mnagnosis.mixin.core;

import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityCollisionAccess;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityCollisionSolver;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityFrame;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityMoveResult;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityPhysics;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityTransitionFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class EntityGravityShiftMixin implements GravityCollisionAccess {

    @Unique
    private GravityMoveResult mnagnosis$lastMove;
    @Unique
    private Vec3 mnagnosis$velocityBeforeCollisionResponse = Vec3.ZERO;

    @Override
    public GravityMoveResult mnagnosis$gravityMoveResult() {
        return mnagnosis$lastMove;
    }

    @Shadow
    public abstract Vec3 position();

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract EntityDimensions getDimensions(Pose pose);

    @Shadow
    public abstract float getEyeHeight();

    @Shadow
    public abstract float getYRot();

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public abstract void setDeltaMovement(Vec3 movement);

    @Shadow
    public abstract void setOnGround(boolean onGround);

    @Shadow
    public abstract AABB getBoundingBox();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract boolean onGround();

    @Inject(method = "makeBoundingBox", at = @At("RETURN"), cancellable = true)
    private void mnagnosis$rotateBoundingBox(
            CallbackInfoReturnable<AABB> callback
    ) {
        Entity self = (Entity) (Object) this;
        GravityDirection gravity = GravityShiftApi.direction(self);
        if (gravity == GravityDirection.DOWN) {
            return;
        }
        EntityDimensions dimensions = getDimensions(getPose());
        callback.setReturnValue(GravityFrame.anchoredBox(
                position(), dimensions.width, dimensions.height, gravity
        ));
    }

    @Inject(
            method = "getBoundingBoxForPose",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mnagnosis$rotatePoseBoundingBox(
            Pose pose,
            CallbackInfoReturnable<AABB> callback
    ) {
        Entity self = (Entity) (Object) this;
        GravityDirection gravity = GravityShiftApi.direction(self);
        if (gravity == GravityDirection.DOWN) {
            return;
        }
        EntityDimensions dimensions = getDimensions(pose);
        callback.setReturnValue(GravityFrame.anchoredBox(
                position(), dimensions.width, dimensions.height, gravity
        ));
    }

    @Inject(method = "getEyePosition()Lnet/minecraft/world/phys/Vec3;",
            at = @At("RETURN"), cancellable = true)
    private void mnagnosis$rotateEyePosition(
            CallbackInfoReturnable<Vec3> callback
    ) {
        GravityDirection gravity = GravityShiftApi.direction((Entity) (Object) this);
        if (gravity != GravityDirection.DOWN) {
            callback.setReturnValue(GravityPhysics.eyePosition(
                    position(), getEyeHeight(), gravity
            ));
        }
    }

    @Inject(method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;",
            at = @At("RETURN"), cancellable = true)
    private void mnagnosis$rotateInterpolatedEyePosition(
            float partialTick,
            CallbackInfoReturnable<Vec3> callback
    ) {
        Entity self = (Entity) (Object) this;
        GravityDirection gravity = GravityShiftApi.direction(self);
        if (gravity != GravityDirection.DOWN) {
            callback.setReturnValue(GravityPhysics.eyePosition(
                    self.getPosition(partialTick), getEyeHeight(), gravity
            ));
        }
    }

    @Inject(method = "getLookAngle", at = @At("RETURN"), cancellable = true)
    private void mnagnosis$rotateLookVector(
            CallbackInfoReturnable<Vec3> callback
    ) {
        GravityDirection gravity = GravityShiftApi.direction((Entity) (Object) this);
        if (gravity != GravityDirection.DOWN) {
            callback.setReturnValue(gravity.toWorld(callback.getReturnValue()));
        }
    }

    @Inject(method = "getOnPosLegacy", at = @At("HEAD"), cancellable = true)
    private void mnagnosis$getGravitySupportBlock(
            CallbackInfoReturnable<BlockPos> callback
    ) {
        GravityDirection gravity = GravityShiftApi.direction(
                (Entity) (Object) this
        );
        if (gravity != GravityDirection.DOWN) {
            callback.setReturnValue(BlockPos.containing(
                    position().add(gravity.downVector().scale(0.2D))
            ));
        }
    }

    @Inject(method = "getOnPos()Lnet/minecraft/core/BlockPos;",
            at = @At("HEAD"), cancellable = true)
    private void mnagnosis$getGravityStepBlock(
            CallbackInfoReturnable<BlockPos> callback
    ) {
        GravityDirection gravity = GravityShiftApi.direction(
                (Entity) (Object) this
        );
        if (gravity != GravityDirection.DOWN) {
            callback.setReturnValue(BlockPos.containing(
                    position().add(gravity.downVector().scale(1.0E-5D))
            ));
        }
    }

    @Inject(
            method = "getBlockPosBelowThatAffectsMyMovement",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mnagnosis$getGravityFrictionBlock(
            CallbackInfoReturnable<BlockPos> callback
    ) {
        GravityDirection gravity = GravityShiftApi.direction(
                (Entity) (Object) this
        );
        if (gravity != GravityDirection.DOWN) {
            callback.setReturnValue(BlockPos.containing(
                    position().add(gravity.downVector().scale(0.500001D))
            ));
        }
    }

    @Inject(method = "moveRelative", at = @At("HEAD"), cancellable = true)
    private void mnagnosis$moveRelativeToGravity(
            float amount,
            Vec3 relative,
            CallbackInfo callback
    ) {
        Entity self = (Entity) (Object) this;
        GravityDirection gravity = GravityShiftApi.direction(self);
        var state = GravityShiftApi.state(self);
        boolean transitioning = state != null && state.transitionTicks() > 0;
        if (gravity == GravityDirection.DOWN && !transitioning) {
            return;
        }
        double lengthSquared = relative.lengthSqr();
        if (lengthSquared < 1.0E-7D) {
            callback.cancel();
            return;
        }
        Vec3 normalized = (lengthSquared > 1.0D
                ? relative.normalize() : relative).scale(amount);
        float sine = Mth.sin(getYRot() * Mth.DEG_TO_RAD);
        float cosine = Mth.cos(getYRot() * Mth.DEG_TO_RAD);
        Vec3 vanillaFrame = new Vec3(
                normalized.x * cosine - normalized.z * sine,
                normalized.y,
                normalized.z * cosine + normalized.x * sine
        );
        Vec3 worldFrame = transitioning
                ? GravityTransitionFrame.control(
                        vanillaFrame,
                        GravityTransitionFrame.rotation(
                                state.transitionOriginRotation(),
                                state.direction(),
                                state.transitionTicks(),
                                0.0F
                        ),
                        gravity,
                        true
                )
                : gravity.toWorld(vanillaFrame);
        setDeltaMovement(getDeltaMovement().add(worldFrame));
        callback.cancel();
    }

    @Inject(method = "move", at = @At("HEAD"))
    private void mnagnosis$captureMovement(
            MoverType moverType,
            Vec3 movement,
            CallbackInfo callback
    ) {
        mnagnosis$lastMove = null;
    }

    /**
     * Vanilla's collider always treats world Y as vertical. For shifted living
     * entities, resolve the same shapes in gravity-local axis order instead.
     */
    @Inject(method = "collide", at = @At("HEAD"), cancellable = true)
    private void mnagnosis$collideInGravityFrame(
            Vec3 movement,
            CallbackInfoReturnable<Vec3> callback
    ) {
        Entity self = (Entity) (Object) this;
        GravityDirection gravity = GravityShiftApi.direction(self);
        if (!(self instanceof LivingEntity)) {
            mnagnosis$lastMove = null;
            return;
        }
        var state = GravityShiftApi.state(self);
        if (gravity == GravityDirection.DOWN
                && (state == null || !state.hasMobileAdhesion())) {
            mnagnosis$lastMove = null;
            return;
        }

        AABB box = getBoundingBox();
        AABB sweep = box.expandTowards(movement).minmax(
                box.expandTowards(gravity.upVector().scale(
                        self.getStepHeight()
                ))
        );
        List<VoxelShape> shapes = new ArrayList<>(
                level().getEntityCollisions(self, sweep)
        );
        for (VoxelShape shape : level().getBlockCollisions(self, sweep)) {
            shapes.add(shape);
        }
        if (level().getWorldBorder().isInsideCloseToBorder(self, sweep)) {
            shapes.add(level().getWorldBorder().getCollisionShape());
        }

        mnagnosis$lastMove = GravityCollisionSolver.solve(
                box,
                movement,
                gravity,
                self.getStepHeight(),
                onGround(),
                shapes
        );
        callback.setReturnValue(mnagnosis$lastMove.actualWorld());
    }

    /*
     * Positioning above this point remains world-space. The rest of
     * Entity.move computes grounded/collision/fall state, so feed that section
     * gravity-local vectors just as vanilla expects.
     */
    @ModifyVariable(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    ordinal = 0
            ),
            ordinal = 0,
            argsOnly = true
    )
    private Vec3 mnagnosis$requestedMovementForVanillaState(Vec3 movement) {
        GravityDirection gravity = GravityShiftApi.direction(
                (Entity) (Object) this
        );
        return gravity == GravityDirection.DOWN
                ? movement : gravity.toLocal(movement);
    }

    @ModifyVariable(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V",
                    ordinal = 0
            ),
            ordinal = 1
    )
    private Vec3 mnagnosis$actualMovementForVanillaState(Vec3 movement) {
        GravityDirection gravity = GravityShiftApi.direction(
                (Entity) (Object) this
        );
        return gravity == GravityDirection.DOWN
                ? movement : gravity.toLocal(movement);
    }

    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            )
    )
    private void mnagnosis$captureWorldVelocityBeforeCollisionResponse(
            MoverType moverType,
            Vec3 movement,
            CallbackInfo callback
    ) {
        mnagnosis$velocityBeforeCollisionResponse = getDeltaMovement();
    }

    @Inject(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )
    )
    private void mnagnosis$restoreGravityRelativeCollisionResponse(
            MoverType moverType,
            Vec3 movement,
            CallbackInfo callback
    ) {
        Entity self = (Entity) (Object) this;
        GravityDirection gravity = GravityShiftApi.direction(self);
        if (gravity == GravityDirection.DOWN || mnagnosis$lastMove == null) {
            return;
        }
        Vec3 local = gravity.toLocal(
                mnagnosis$velocityBeforeCollisionResponse
        );
        if (Math.abs(mnagnosis$lastMove.requestedLocal().x
                - mnagnosis$lastMove.actualLocal().x) > 1.0E-7D) {
            local = new Vec3(0.0D, local.y, local.z);
        }
        if (Math.abs(mnagnosis$lastMove.requestedLocal().z
                - mnagnosis$lastMove.actualLocal().z) > 1.0E-7D) {
            local = new Vec3(local.x, local.y, 0.0D);
        }
        setDeltaMovement(gravity.toWorld(local));
    }

    @Inject(method = "move", at = @At("TAIL"))
    private void mnagnosis$resolveLocalGround(
            MoverType moverType,
            Vec3 movement,
            CallbackInfo callback
    ) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof LivingEntity)) {
            return;
        }
        GravityDirection gravity = GravityShiftApi.direction(self);
        if (gravity == GravityDirection.DOWN) {
            return;
        }
        if (mnagnosis$lastMove != null) {
            setOnGround(mnagnosis$lastMove.grounded());
        }
    }
}
