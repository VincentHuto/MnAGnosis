package com.vincenthuto.mnagnosis.common.particle;

import com.vincenthuto.mnagnosis.common.registry.ParticleRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class IneffableParticleEffects {

    private IneffableParticleEffects() {
    }

    public static IneffableCubeParticleType variant(int sample) {
        return (sample & 1) == 0
                ? ParticleRegistry.INEFFABLE_BLACK_CUBE.get()
                : ParticleRegistry.INEFFABLE_WHITE_CUBE.get();
    }

    public static IneffableCubeParticleOptions options(int sample, float scale) {
        return variant(sample).options(scale);
    }

    public static void add(
            Level level,
            int sample,
            Vec3 position,
            Vec3 velocity
    ) {
        add(level, sample, position, velocity, 1.0F);
    }

    public static void add(
            Level level,
            int sample,
            Vec3 position,
            Vec3 velocity,
            float scale
    ) {
        level.addParticle(
                options(sample, scale),
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
        cloud(
                level, center, countPerVariant,
                spreadX, spreadY, spreadZ, speed,
                1.0F
        );
    }

    public static void cloud(
            ServerLevel level,
            Vec3 center,
            int countPerVariant,
            double spreadX,
            double spreadY,
            double spreadZ,
            double speed,
            float scale
    ) {
        level.sendParticles(
                ParticleRegistry.INEFFABLE_BLACK_CUBE.get().options(scale),
                center.x, center.y, center.z,
                countPerVariant, spreadX, spreadY, spreadZ, speed
        );
        level.sendParticles(
                ParticleRegistry.INEFFABLE_WHITE_CUBE.get().options(scale),
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
        handParticle(level, origin, forward, phase,1);
    }

    public static void handParticle(
            Level level,
            Vec3 origin,
            Vec3 forward,
            long phase,
            float scale
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
        add(level, (int) phase, origin.add(orbit), velocity.multiply(0.1,0.1,0.1), scale*0.5f);
    }
}
