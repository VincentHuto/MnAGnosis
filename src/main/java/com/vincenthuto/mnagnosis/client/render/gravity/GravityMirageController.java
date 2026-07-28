package com.vincenthuto.mnagnosis.client.render.gravity;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import com.vincenthuto.mnagnosis.common.entity.GravityShiftSurfaceEntity;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityMirageMath;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GravityMirageController {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation MIRAGE_EFFECT =
            MnAGnosis.rloc("shaders/post/gravity_mirage.json");
    private static final double MAX_RENDER_DISTANCE_SQUARED = 96.0D * 96.0D;
    private static final float FACE_EPSILON = 0.003F;

    private static PostChain postChain;
    private static int targetWidth;
    private static int targetHeight;
    private static boolean shaderUnavailable;

    private GravityMirageController() {
    }

    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }
        List<GravityShiftSurfaceEntity> surfaces = collect(event);
        if (surfaces.isEmpty()) {
            return;
        }
        RenderTarget main = minecraft.getMainRenderTarget();
        if (!ensurePostChain(minecraft, main)) {
            renderFaces(event, surfaces, false);
            return;
        }
        RenderTarget mask = postChain.getTempTarget("gravity_mask");
        if (mask == null) {
            shaderUnavailable = true;
            renderFaces(event, surfaces, false);
            return;
        }
        mask.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        mask.clear(Minecraft.ON_OSX);
        mask.copyDepthFrom(main);
        mask.bindWrite(false);
        renderFaces(event, surfaces, true);
        main.bindWrite(false);

        EffectInstance effect = postChain.passes.get(0).getEffect();
        effect.safeGetUniform("Time").set(
                (minecraft.level.getGameTime() + event.getPartialTick()) / 20.0F
        );
        postChain.process(event.getPartialTick());
        main.bindWrite(false);
    }

    public static void reset() {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(GravityMirageController::closeNow);
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
                        MIRAGE_EFFECT
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
                    "Could not load Gravity Shift mirage shader; using face overlay",
                    exception
            );
            return false;
        }
    }

    private static List<GravityShiftSurfaceEntity> collect(
            RenderLevelStageEvent event
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 camera = event.getCamera().getPosition();
        List<GravityShiftSurfaceEntity> surfaces = new ArrayList<>();
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity instanceof GravityShiftSurfaceEntity surface
                    && !surface.isRemoved()
                    && surface.position().distanceToSqr(camera)
                    <= MAX_RENDER_DISTANCE_SQUARED
                    && event.getFrustum().isVisible(
                    surface.getBoundingBox().inflate(surface.getRadius() + 1.0D))) {
                surfaces.add(surface);
            }
        }
        surfaces.sort(Comparator.comparingDouble(
                surface -> surface.position().distanceToSqr(camera)
        ));
        return surfaces;
    }

    private static void renderFaces(
            RenderLevelStageEvent event,
            List<GravityShiftSurfaceEntity> surfaces,
            boolean maskPass
    ) {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        if (maskPass) {
            RenderSystem.disableBlend();
        } else {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.setShader(CoreShaders::gravityMirageMask);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Camera renderCamera = event.getCamera();
        for (GravityShiftSurfaceEntity surface : surfaces) {
            float lifeFade = Math.min(1.0F,
                    Math.max(0.0F, surface.getRemainingTicks() / 10.0F));
            int alpha = maskPass ? 255 : Math.round(80.0F * lifeFade);
            for (BlockPos position : surface.activeFaces()) {
                face(builder, renderCamera, event.getProjectionMatrix(),
                        position, surface.getFace(),
                        225, 245, 255, alpha);
            }
        }
        BufferUploader.drawWithShader(builder.end());
        if (!maskPass) {
            RenderSystem.disableBlend();
        }
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }

    private static void face(
            BufferBuilder builder,
            Camera camera,
            Matrix4f projection,
            BlockPos position,
            Direction direction,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        float x0 = position.getX();
        float y0 = position.getY();
        float z0 = position.getZ();
        float x1 = x0 + 1.0F;
        float y1 = y0 + 1.0F;
        float z1 = z0 + 1.0F;
        switch (direction) {
            case DOWN -> quad(builder, camera, projection,
                    x0, y0 - FACE_EPSILON, z0, x1, y0 - FACE_EPSILON, z0,
                    x1, y0 - FACE_EPSILON, z1, x0, y0 - FACE_EPSILON, z1,
                    red, green, blue, alpha);
            case UP -> quad(builder, camera, projection,
                    x0, y1 + FACE_EPSILON, z1, x1, y1 + FACE_EPSILON, z1,
                    x1, y1 + FACE_EPSILON, z0, x0, y1 + FACE_EPSILON, z0,
                    red, green, blue, alpha);
            case NORTH -> quad(builder, camera, projection,
                    x1, y0, z0 - FACE_EPSILON, x0, y0, z0 - FACE_EPSILON,
                    x0, y1, z0 - FACE_EPSILON, x1, y1, z0 - FACE_EPSILON,
                    red, green, blue, alpha);
            case SOUTH -> quad(builder, camera, projection,
                    x0, y0, z1 + FACE_EPSILON, x1, y0, z1 + FACE_EPSILON,
                    x1, y1, z1 + FACE_EPSILON, x0, y1, z1 + FACE_EPSILON,
                    red, green, blue, alpha);
            case WEST -> quad(builder, camera, projection,
                    x0 - FACE_EPSILON, y0, z0, x0 - FACE_EPSILON, y0, z1,
                    x0 - FACE_EPSILON, y1, z1, x0 - FACE_EPSILON, y1, z0,
                    red, green, blue, alpha);
            case EAST -> quad(builder, camera, projection,
                    x1 + FACE_EPSILON, y0, z1, x1 + FACE_EPSILON, y0, z0,
                    x1 + FACE_EPSILON, y1, z0, x1 + FACE_EPSILON, y1, z1,
                    red, green, blue, alpha);
        }
    }

    private static void quad(
            BufferBuilder builder,
            Camera camera,
            Matrix4f projection,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz,
            int red, int green, int blue, int alpha
    ) {
        Vector3f first = project(camera, projection, ax, ay, az);
        Vector3f second = project(camera, projection, bx, by, bz);
        Vector3f third = project(camera, projection, cx, cy, cz);
        Vector3f fourth = project(camera, projection, dx, dy, dz);
        if (first == null || second == null || third == null || fourth == null) {
            return;
        }
        vertex(builder, first, red, green, blue, alpha);
        vertex(builder, second, red, green, blue, alpha);
        vertex(builder, third, red, green, blue, alpha);
        vertex(builder, fourth, red, green, blue, alpha);
    }

    private static Vector3f project(
            Camera camera,
            Matrix4f projection,
            float x,
            float y,
            float z
    ) {
        return GravityMirageMath.projectToNdc(
                new Vec3(x, y, z),
                camera.getPosition(),
                camera.getLookVector(),
                camera.getUpVector(),
                camera.getLeftVector(),
                projection
        );
    }

    private static void vertex(
            BufferBuilder builder,
            Vector3f position,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        builder.vertex(position.x(), position.y(), position.z())
                .color(red, green, blue, alpha)
                .endVertex();
    }
}
