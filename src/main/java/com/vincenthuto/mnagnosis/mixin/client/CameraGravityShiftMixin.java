package com.vincenthuto.mnagnosis.mixin.client;

import com.vincenthuto.mnagnosis.client.gravity.GravityVisuals;
import com.vincenthuto.mnagnosis.client.gravity.GravityCameraFrame;
import com.vincenthuto.mnagnosis.client.gravity.GravityCameraClearance;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public abstract class CameraGravityShiftMixin {

    @Shadow
    private Entity entity;

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f left;

    @Shadow
    public abstract Vec3 getPosition();

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Invoker("getMaxZoom")
    protected abstract double mnagnosis$getMaxZoom(double desiredZoom);

    @Inject(method = "setup", at = @At("TAIL"))
    private void mnagnosis$rotateCamera(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean mirror,
            float partialTick,
            CallbackInfo callback
    ) {
        GravityDirection gravity = GravityShiftApi.direction(entity);
        if (gravity == GravityDirection.DOWN
                && GravityShiftApi.state(entity) == null) {
            return;
        }
        Quaternionf gravityRotation = GravityVisuals.rotation(entity, partialTick);
        Vec3 rotatedEye = GravityVisuals.eye(entity, partialTick);
        rotation.set(GravityCameraFrame.cameraRotation(
                gravityRotation, rotation
        ));
        GravityCameraFrame.Basis basis = GravityCameraFrame.basis(rotation);
        forwards.set(basis.forward());
        up.set(basis.up());
        left.set(basis.left());
        Vec3 cameraEye = rotatedEye;
        if (level instanceof CollisionGetter collisions) {
            cameraEye = GravityCameraClearance.resolve(
                    collisions, entity, rotatedEye, gravity
            );
        }
        setPosition(cameraEye);
        if (detached) {
            double safeZoom = mnagnosis$getMaxZoom(4.0D);
            setPosition(cameraEye.add(
                    GravityCameraFrame.thirdPersonOffset(basis, safeZoom)
            ));
        }
    }
}
