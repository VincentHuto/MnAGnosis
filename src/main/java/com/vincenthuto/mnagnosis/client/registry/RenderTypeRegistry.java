package com.vincenthuto.mnagnosis.client.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.client.renderer.RenderType;

public class RenderTypeRegistry extends RenderType {
	private static final RenderType CRIMSON_GLINT = create("glint_direct", DefaultVertexFormat.POSITION_TEX,
			Mode.QUADS, 256, false, false,
			CompositeState.builder().setShaderState(RENDERTYPE_GLINT_DIRECT_SHADER)
					.setTextureState(new TextureStateShard(
							MnAGnosis.rloc("textures/item/crimson_item_glint.png"), true, false))
					.setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST)
					.setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(GLINT_TEXTURING)
					.createCompositeState(false));


	public static RenderType getCrimsonGlint() {
		return CRIMSON_GLINT;
	}

	public RenderTypeRegistry(String nameIn, VertexFormat formatIn, Mode drawModeIn, int bufferSizeIn,
			boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

}
