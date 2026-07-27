package com.vincenthuto.mnagnosis.common.particle;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public final class IneffableCubeParticleOptions implements ParticleOptions {

    private final IneffableCubeParticleType type;
    private final float scale;

    IneffableCubeParticleOptions(
            IneffableCubeParticleType type,
            float scale
    ) {
        this.type = type;
        this.scale = normalizeScale(scale);
    }

    public float scale() {
        return scale;
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFloat(scale);
    }

    @Override
    public String writeToString() {
        return String.format(
                Locale.ROOT,
                "%s %.4f",
                ForgeRegistries.PARTICLE_TYPES.getKey(type),
                scale
        );
    }

    private static float normalizeScale(float scale) {
        return Float.isFinite(scale) && scale > 0.0F ? scale : 1.0F;
    }
}
