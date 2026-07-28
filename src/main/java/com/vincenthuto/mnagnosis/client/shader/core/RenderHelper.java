package com.vincenthuto.mnagnosis.client.shader.core;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.mixin.core.RenderTypeAccessor;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class RenderHelper extends RenderType {
    private RenderHelper(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
        super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
        throw new UnsupportedOperationException("Should not be instantiated");
    }

    private static final RenderType DOPPLEGANGER = makeLayer("mnagnosis:doppleganger", DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS, 256, true, true,
            RenderType.CompositeState.builder()
                    .setShaderState(new ShaderStateShard(CoreShaders::doppleganger))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .createCompositeState(true));

    /* The shader generates the armor surface itself. It intentionally has no
     * texture state, so a mask image cannot affect coverage or color. */
    private static final Function<ResourceLocation, RenderType> NOISE = Util.memoize(texture -> {
        CompositeState glState = RenderType.CompositeState.builder()
                .setShaderState(new ShaderStateShard(CoreShaders::noise))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
        return makeLayer("mnagnosis:noise", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, glState);
    });

    private static final Function<ResourceLocation, RenderType> TRUTH_GLITCH = Util.memoize(texture -> {
        CompositeState glState = RenderType.CompositeState.builder()
                .setShaderState(new ShaderStateShard(CoreShaders::truthGlitch))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .createCompositeState(true);
        return makeLayer("mnagnosis:truth_glitch", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, glState);
    });

    public static RenderType getDopplegangerLayer() {
        return DOPPLEGANGER;
    }

    public static RenderType getNoiseLayer(ResourceLocation texture) {
        return NOISE.apply(texture);
    }

    /** Finale progress is carried in vertex alpha, avoiding a shared mutable shader uniform. */
    public static RenderType getTruthGlitchLayer(ResourceLocation texture) {
        return TRUTH_GLITCH.apply(texture);
    }
    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufSize, boolean hasCrumbling, boolean sortOnUpload, CompositeState glState) {
        return RenderTypeAccessor.create(name, format, mode, bufSize, hasCrumbling, sortOnUpload, glState);
    }

    private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode,
                                        int bufSize, CompositeState glState) {
        return makeLayer(name, format, mode, bufSize, false, false, glState);
    }
}
