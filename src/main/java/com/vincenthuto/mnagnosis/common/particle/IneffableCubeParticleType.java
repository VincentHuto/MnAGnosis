package com.vincenthuto.mnagnosis.common.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public final class IneffableCubeParticleType
        extends ParticleType<IneffableCubeParticleOptions> {

    private static final ParticleOptions.Deserializer<IneffableCubeParticleOptions>
            DESERIALIZER =
            new ParticleOptions.Deserializer<>() {
                @Override
                public IneffableCubeParticleOptions fromCommand(
                        ParticleType<IneffableCubeParticleOptions> type,
                        StringReader reader
                ) throws CommandSyntaxException {
                    reader.expect(' ');
                    return cubeType(type).options(reader.readFloat());
                }

                @Override
                public IneffableCubeParticleOptions fromNetwork(
                        ParticleType<IneffableCubeParticleOptions> type,
                        FriendlyByteBuf buffer
                ) {
                    return cubeType(type).options(buffer.readFloat());
                }
            };

    private final Codec<IneffableCubeParticleOptions> codec;

    public IneffableCubeParticleType() {
        super(false, DESERIALIZER);
        this.codec = Codec.FLOAT.xmap(
                this::options,
                IneffableCubeParticleOptions::scale
        );
    }

    public IneffableCubeParticleOptions options(float scale) {
        return new IneffableCubeParticleOptions(this, scale);
    }

    @Override
    public Codec<IneffableCubeParticleOptions> codec() {
        return codec;
    }

    private static IneffableCubeParticleType cubeType(
            ParticleType<IneffableCubeParticleOptions> type
    ) {
        return (IneffableCubeParticleType) type;
    }
}
