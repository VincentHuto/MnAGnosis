package com.vincenthuto.mnagnosis.client.authorship;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class IneffableHudCubeRenderer {

    private static final ResourceLocation WHITE_TEXTURE = MnAGnosis.rloc(
            "textures/particle/ineffable_white_cube.png"
    );
    private static final ResourceLocation BLACK_TEXTURE = MnAGnosis.rloc(
            "textures/particle/ineffable_black_cube.png"
    );
    private static final int[][] FACES = {
            {0, 3, 2, 1}, {4, 5, 6, 7},
            {0, 4, 7, 3}, {1, 2, 6, 5},
            {0, 1, 5, 4}, {3, 7, 6, 2}
    };
    private static final float[][] UV_CORNERS = {
            {0.0F, 1.0F}, {0.0F, 0.0F},
            {1.0F, 0.0F}, {1.0F, 1.0F}
    };
    private static final float OVERLAY_Z = 0.0F;

    private IneffableHudCubeRenderer() {
    }

    public static void render(
            GuiGraphics graphics,
            float frameX,
            float frameY,
            float animationTime
    ) {
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        try {
            renderVariant(
                    graphics,
                    frameX,
                    frameY,
                    animationTime,
                    IneffableHudCubeLayout.TextureVariant.WHITE,
                    WHITE_TEXTURE
            );
            renderVariant(
                    graphics,
                    frameX,
                    frameY,
                    animationTime,
                    IneffableHudCubeLayout.TextureVariant.BLACK,
                    BLACK_TEXTURE
            );
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }

    private static void renderVariant(
            GuiGraphics graphics,
            float frameX,
            float frameY,
            float animationTime,
            IneffableHudCubeLayout.TextureVariant variant,
            ResourceLocation texture
    ) {
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR
        );

        Matrix4f matrix = graphics.pose().last().pose();
        for (int index = 0;
             index < IneffableHudCubeLayout.anchors().size();
             index++) {
            IneffableHudCubeLayout.Sample sample =
                    IneffableHudCubeLayout.sample(index, animationTime);
            if (sample.texture() == variant) {
                emitCube(builder, matrix, frameX, frameY, sample);
            }
        }
        BufferUploader.drawWithShader(builder.end());
    }

    private static void emitCube(
            BufferBuilder builder,
            Matrix4f matrix,
            float frameX,
            float frameY,
            IneffableHudCubeLayout.Sample sample
    ) {
        Quaternionf rotation = new Quaternionf().rotationXYZ(
                sample.rotationX(),
                sample.rotationY(),
                sample.rotationZ()
        );
        Vector3f[] corners = {
                corner(-1, -1, -1, sample.halfSize(), rotation),
                corner(1, -1, -1, sample.halfSize(), rotation),
                corner(1, 1, -1, sample.halfSize(), rotation),
                corner(-1, 1, -1, sample.halfSize(), rotation),
                corner(-1, -1, 1, sample.halfSize(), rotation),
                corner(1, -1, 1, sample.halfSize(), rotation),
                corner(1, 1, 1, sample.halfSize(), rotation),
                corner(-1, 1, 1, sample.halfSize(), rotation)
        };

        for (int[] face : FACES) {
            for (int vertex = 0; vertex < face.length; vertex++) {
                Vector3f point = corners[face[vertex]];
                builder.vertex(
                                matrix,
                                frameX + sample.x() + point.x,
                                frameY + sample.y() + point.y,
                                OVERLAY_Z + point.z
                        )
                        .uv(
                                UV_CORNERS[vertex][0],
                                UV_CORNERS[vertex][1]
                        )
                        .color(1.0F, 1.0F, 1.0F, sample.alpha())
                        .endVertex();
            }
        }
    }

    private static Vector3f corner(
            int x,
            int y,
            int z,
            float halfSize,
            Quaternionf rotation
    ) {
        return new Vector3f(x * halfSize, y * halfSize, z * halfSize)
                .rotate(rotation);
    }
}
