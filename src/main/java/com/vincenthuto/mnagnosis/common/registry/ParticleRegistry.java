package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.particle.IneffableCubeParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MnAGnosis.MODID);

    public static final RegistryObject<IneffableCubeParticleType>
            INEFFABLE_BLACK_CUBE = PARTICLES.register(
                    "ineffable_black_cube", IneffableCubeParticleType::new
            );
    public static final RegistryObject<IneffableCubeParticleType>
            INEFFABLE_WHITE_CUBE = PARTICLES.register(
                    "ineffable_white_cube", IneffableCubeParticleType::new
            );

    private ParticleRegistry() {
    }
}
