package com.vincenthuto.mnagnosis.common.particle;

import com.vincenthuto.mnagnosis.common.registry.ParticleRegistry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class IneffableParticleEffects {

    private IneffableParticleEffects() {
    }

    public static SimpleParticleType variant(int sample) {
        return (sample & 1) == 0
                ? ParticleRegistry.INEFFABLE_BLACK_CUBE.get()
                : ParticleRegistry.INEFFABLE_WHITE_CUBE.get();
    }

    public static void add(
            Level level,
            int sample,
            Vec3 position,
            Vec3 velocity
    ) {
        level.addParticle(
                variant(sample),
                position.x, position.y, position.z,
                velocity.x, velocity.y, velocity.z
        );
    }

    public static void cloud(
            ServerLevel level,
            Vec3 center,
            int countPerVariant,
            double spreadX,
            double spreadY,
            double spreadZ,
            double speed
    ) {
        level.sendParticles(
                ParticleRegistry.INEFFABLE_BLACK_CUBE.get(),
                center.x, center.y, center.z,
                countPerVariant, spreadX, spreadY, spreadZ, speed
        );
        level.sendParticles(
                ParticleRegistry.INEFFABLE_WHITE_CUBE.get(),
                center.x, center.y, center.z,
                countPerVariant, spreadX, spreadY, spreadZ, speed
        );
    }

    public static void handParticle(
            Level level,
            Vec3 origin,
            Vec3 forward,
            long phase
    ) {
        RandomSource random = level.random;
        double angle = phase * 0.43D + random.nextDouble() * 0.35D;
        Vec3 orbit = new Vec3(
                Math.cos(angle) * 0.09D,
                Math.sin(angle * 1.7D) * 0.07D,
                Math.sin(angle) * 0.09D
        );
        Vec3 velocity = forward.scale(0.018D).add(
                (random.nextDouble() - 0.5D) * 0.008D,
                0.008D + random.nextDouble() * 0.008D,
                (random.nextDouble() - 0.5D) * 0.008D
        );
        add(level, (int) phase, origin.add(orbit), velocity);
    }
}
