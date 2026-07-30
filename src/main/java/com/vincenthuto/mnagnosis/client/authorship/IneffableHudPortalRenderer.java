package com.vincenthuto.mnagnosis.client.authorship;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.mnagnosis.client.shader.core.CoreShaders;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public final class IneffableHudPortalRenderer {

    public static final int X = IneffableHudConcept.CHANNEL_X;
    public static final int Y = IneffableHudConcept.CHANNEL_Y;
    public static final int WIDTH = IneffableHudConcept.CHANNEL_WIDTH-30;
    public static final int HEIGHT = IneffableHudConcept.CHANNEL_HEIGHT;

    private static final float OPACITY = 0.88F;

    private IneffableHudPortalRenderer() {
    }

    public static float animationSeconds(
            long gameTime,
            float partialTick
    ) {
        return (gameTime + partialTick) / 20.0F;
    }

    public static void render(
            GuiGraphics graphics,
            float animationTicks
    ) {
        ShaderInstance shader = CoreShaders.ineffableHudPortal();
        if (shader == null) {
            return;
        }

        graphics.flush();
        shader.safeGetUniform("PortalTime").set(animationTicks / 20.0F);
        shader.safeGetUniform("PortalOpacity").set(OPACITY);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);

        try {
            Matrix4f matrix = graphics.pose().last().pose();
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            builder.begin(
                    VertexFormat.Mode.QUADS,
                    DefaultVertexFormat.POSITION_TEX
            );
            builder.vertex(matrix, X, Y + HEIGHT, 0.0F)
                    .uv(0.0F, 1.0F)
                    .endVertex();
            builder.vertex(matrix, X + WIDTH, Y + HEIGHT, 0.0F)
                    .uv(1.0F, 1.0F)
                    .endVertex();
            builder.vertex(matrix, X + WIDTH, Y, 0.0F)
                    .uv(1.0F, 0.0F)
                    .endVertex();
            builder.vertex(matrix, X, Y, 0.0F)
                    .uv(0.0F, 0.0F)
                    .endVertex();
            BufferUploader.drawWithShader(builder.end());
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }
}
