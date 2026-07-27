package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MnAGnosis.MODID);

    public static final RegistryObject<SimpleParticleType> INEFFABLE_BLACK_CUBE =
            PARTICLES.register("ineffable_black_cube", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> INEFFABLE_WHITE_CUBE =
            PARTICLES.register("ineffable_white_cube", () -> new SimpleParticleType(false));

    private ParticleRegistry() {
    }
}
