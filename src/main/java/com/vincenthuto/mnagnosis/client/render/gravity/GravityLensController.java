package com.vincenthuto.mnagnosis.client.render.gravity;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.render.entity.GravityFieldRenderer;
import com.vincenthuto.mnagnosis.common.entity.GravityFieldEntity;
import com.vincenthuto.mnagnosis.common.spell.gravity.GravityLensMath;
import com.vincenthuto.mnagnosis.common.spell.gravity.GravityPolarity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GravityLensController {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation LENS_EFFECT =
            MnAGnosis.rloc("shaders/post/gravity_lens.json");
    private static final String[] LENS_UNIFORMS = {"Lens0", "Lens1", "Lens2"};
    private static final float VIEWPORT_MARGIN = 0.25F;
    private static final double VISUAL_CENTER_Y_OFFSET = 1.5D;

    private static PostChain postChain;
    private static int targetWidth;
    private static int targetHeight;
    private static boolean shaderUnavailable;

    private GravityLensController() {
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        RenderTarget target = minecraft.getMainRenderTarget();
        List<Lens> lenses = collectLenses(event, target.viewWidth, target.viewHeight);
        if (lenses.isEmpty() || !ensurePostChain(minecraft, target)) {
            return;
        }

        EffectInstance effect = postChain.passes.get(0).getEffect();
        for (int index = 0; index < LENS_UNIFORMS.length; index++) {
            if (index < lenses.size()) {
                Lens lens = lenses.get(index);
                effect.safeGetUniform(LENS_UNIFORMS[index]).set(
                        lens.screenX(),
                        lens.screenY(),
                        lens.screenRadius(),
                        lens.polarity()
                );
            } else {
                effect.safeGetUniform(LENS_UNIFORMS[index])
                        .set(0.0F, 0.0F, 0.0F, 0.0F);
            }
        }
        postChain.process(event.getPartialTick());
        target.bindWrite(false);
    }

    public static void reset() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(GravityLensController::closeNow);
            return;
        }
        closeNow();
    }

    private static void closeNow() {
        if (postChain != null) {
            postChain.close();
            postChain = null;
        }
        targetWidth = 0;
        targetHeight = 0;
        shaderUnavailable = false;
    }

    private static boolean ensurePostChain(
            Minecraft minecraft,
            RenderTarget target
    ) {
        if (shaderUnavailable) {
            return false;
        }
        try {
            if (postChain == null) {
                postChain = new PostChain(
                        minecraft.getTextureManager(),
                        minecraft.getResourceManager(),
                        target,
                        LENS_EFFECT
                );
            }
            if (target.width != targetWidth || target.height != targetHeight) {
                postChain.resize(target.width, target.height);
                targetWidth = target.width;
                targetHeight = target.height;
            }
            return true;
        } catch (IOException | RuntimeException exception) {
            shaderUnavailable = true;
            if (postChain != null) {
                postChain.close();
                postChain = null;
            }
            LOGGER.error(
                    "Could not load Gravity Convergence lens shader; "
                            + "event-horizon geometry will remain active",
                    exception
            );
            return false;
        }
    }

    private static List<Lens> collectLenses(
            RenderLevelStageEvent event,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.getPosition();
        List<GravityFieldEntity> fields = new ArrayList<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof GravityFieldEntity field
                    && !field.isRemoved()
                    && event.getFrustum().isVisible(
                    field.getBoundingBox()
                            .move(0.0D, VISUAL_CENTER_Y_OFFSET, 0.0D)
                            .inflate(
                            GravityFieldRenderer.horizonRadius(field.getRadius())
                                    * GravityLensMath.HALO_RADIUS
                    ))
                    && hasLineOfSight(minecraft, camera, field)) {
                fields.add(field);
            }
        }
        fields.sort(Comparator.comparingDouble(
                field -> visualCenter(field, event.getPartialTick())
                        .distanceToSqr(cameraPosition)
        ));

        List<Lens> lenses = new ArrayList<>(LENS_UNIFORMS.length);
        for (GravityFieldEntity field : fields) {
            Lens lens = project(
                    field, event.getPartialTick(), camera,
                    event.getProjectionMatrix(), screenWidth, screenHeight
            );
            if (lens != null) {
                lenses.add(lens);
                if (lenses.size() == LENS_UNIFORMS.length) {
                    break;
                }
            }
        }
        return lenses;
    }

    private static boolean hasLineOfSight(
            Minecraft minecraft,
            Camera camera,
            GravityFieldEntity field
    ) {
        Vec3 cameraPosition = camera.getPosition();
        Vec3 fieldPosition = visualCenter(field, 1.0F);
        HitResult hit = minecraft.level.clip(new ClipContext(
                cameraPosition,
                fieldPosition,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                camera.getEntity()
        ));
        return hit.getType() == HitResult.Type.MISS
                || hit.getLocation().distanceToSqr(cameraPosition) + 0.16D
                >= fieldPosition.distanceToSqr(cameraPosition);
    }

    private static Lens project(
            GravityFieldEntity field,
            float partialTick,
            Camera camera,
            Matrix4f projection,
            int screenWidth,
            int screenHeight
    ) {
        Vec3 center = visualCenter(field, partialTick);
        ScreenPoint projectedCenter = projectPoint(center, camera, projection);
        if (projectedCenter == null
                || projectedCenter.x() < -VIEWPORT_MARGIN
                || projectedCenter.x() > 1.0F + VIEWPORT_MARGIN
                || projectedCenter.y() < -VIEWPORT_MARGIN
                || projectedCenter.y() > 1.0F + VIEWPORT_MARGIN) {
            return null;
        }

        float horizon = GravityFieldRenderer.horizonRadius(field.getRadius());
        Vector3f up = camera.getUpVector();
        ScreenPoint projectedEdge = projectPoint(
                center.add(up.x() * horizon, up.y() * horizon, up.z() * horizon),
                camera,
                projection
        );
        if (projectedEdge == null) {
            return null;
        }
        float radius = GravityLensMath.clampScreenRadius(
                Math.abs(projectedEdge.y() - projectedCenter.y()) * screenHeight
        );
        float polarity =
                field.getPolarity() == GravityPolarity.REPEL ? -1.0F : 1.0F;
        return new Lens(
                projectedCenter.x(),
                projectedCenter.y(),
                radius,
                polarity
        );
    }

    private static Vec3 visualCenter(
            GravityFieldEntity field,
            float partialTick
    ) {
        return new Vec3(
                Mth.lerp(partialTick, field.xOld, field.getX()),
                Mth.lerp(partialTick, field.yOld, field.getY())
                        + VISUAL_CENTER_Y_OFFSET,
                Mth.lerp(partialTick, field.zOld, field.getZ())
        );
    }

    private static ScreenPoint projectPoint(
            Vec3 worldPosition,
            Camera camera,
            Matrix4f projection
    ) {
        Vec3 relative = worldPosition.subtract(camera.getPosition());
        Vector3f viewPosition = new Vector3f(
                (float) relative.x,
                (float) relative.y,
                (float) relative.z
        );
        new Quaternionf(camera.rotation()).conjugate().transform(viewPosition);
        Vector4f clip = new Vector4f(viewPosition, 1.0F);
        projection.transform(clip);
        if (!Float.isFinite(clip.w()) || clip.w() <= 1.0E-4F) {
            return null;
        }
        float inverseW = 1.0F / clip.w();
        float x = clip.x() * inverseW * 0.5F + 0.5F;
        float y = clip.y() * inverseW * 0.5F + 0.5F;
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            return null;
        }
        return new ScreenPoint(x, y);
    }

    private record ScreenPoint(float x, float y) {
    }

    private record Lens(
            float screenX,
            float screenY,
            float screenRadius,
            float polarity
    ) {
    }
}
