/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package com.vincenthuto.mnagnosis.client.shader.core;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.function.Consumer;

public class CoreShaders {
    private static ShaderInstance doppleganger;
    private static ShaderInstance noise;
    private static ShaderInstance truthGlitch;

    public static void init(TriConsumer<ResourceLocation, VertexFormat, Consumer<ShaderInstance>> registrations) {
        registrations.accept(
                MnAGnosis.rloc("doppleganger"),
                DefaultVertexFormat.NEW_ENTITY,
                inst -> doppleganger = inst
        );
        registrations.accept(
                MnAGnosis.rloc("noise"),
                DefaultVertexFormat.NEW_ENTITY,
                inst -> noise = inst
        );
        registrations.accept(
                MnAGnosis.rloc("truth_glitch"),
                DefaultVertexFormat.NEW_ENTITY,
                inst -> truthGlitch = inst
        );

    }

    public static ShaderInstance doppleganger() {
        return doppleganger;
    }


    public static ShaderInstance noise() {
        return noise;
    }

    public static ShaderInstance truthGlitch() {
        return truthGlitch;
    }


}
