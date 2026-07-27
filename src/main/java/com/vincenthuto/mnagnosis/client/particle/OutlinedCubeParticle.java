package com.vincenthuto.mnagnosis.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class OutlinedCubeParticle extends TextureSheetParticle {

    private static final int[][] FACES = {
            {0, 3, 2, 1}, {4, 5, 6, 7},
            {0, 4, 7, 3}, {1, 2, 6, 5},
            {0, 1, 5, 4}, {3, 7, 6, 2}
    };
    private static final float[][] UV_CORNERS = {
            {0.0F, 1.0F}, {0.0F, 0.0F}, {1.0F, 0.0F}, {1.0F, 1.0F}
    };

    private final SpriteSet sprites;
    private final float spinOffset;
    private final float spinRate;

    private OutlinedCubeParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            SpriteSet sprites
    ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.sprites = sprites;
        this.spinOffset = random.nextFloat() * ((float) Math.PI * 2.0F);
        this.spinRate = 0.10F + random.nextFloat() * 0.14F;
        this.lifetime = 18 + random.nextInt(11);
        this.quadSize = 0.075F + random.nextFloat() * 0.045F;
        this.friction = 0.92F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void render(VertexConsumer consumer, Camera camera, float partialTick) {
        Vec3 cameraPosition = camera.getPosition();
        float centerX = (float) (this.xo + (this.x - this.xo) * partialTick
                - cameraPosition.x);
        float centerY = (float) (this.yo + (this.y - this.yo) * partialTick
                - cameraPosition.y);
        float centerZ = (float) (this.zo + (this.z - this.zo) * partialTick
                - cameraPosition.z);

        float life = (this.age + partialTick) / this.lifetime;
        float envelope = Math.min(1.0F, life * 6.0F)
                * Math.min(1.0F, (1.0F - life) * 5.0F);
        float halfSize = this.quadSize * (0.72F + envelope * 0.28F);
        float spin = spinOffset + (this.age + partialTick) * spinRate;
        Quaternionf rotation = new Quaternionf().rotationXYZ(
                spin * 0.73F,
                spin,
                spin * 0.47F
        );

        Vector3f[] corners = {
                corner(-1, -1, -1, halfSize, rotation),
                corner(1, -1, -1, halfSize, rotation),
                corner(1, 1, -1, halfSize, rotation),
                corner(-1, 1, -1, halfSize, rotation),
                corner(-1, -1, 1, halfSize, rotation),
                corner(1, -1, 1, halfSize, rotation),
                corner(1, 1, 1, halfSize, rotation),
                corner(-1, 1, 1, halfSize, rotation)
        };

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        int light = 0xF000F0;
        float alpha = this.alpha * Math.min(1.0F, (1.0F - life) * 4.0F);

        for (int[] face : FACES) {
            for (int vertex = 0; vertex < 4; vertex++) {
                Vector3f point = corners[face[vertex]];
                float u = UV_CORNERS[vertex][0] == 0.0F ? u0 : u1;
                float v = UV_CORNERS[vertex][1] == 0.0F ? v0 : v1;
                consumer.vertex(
                                centerX + point.x,
                                centerY + point.y,
                                centerZ + point.z
                        )
                        .uv(u, v)
                        .color(this.rCol, this.gCol, this.bCol, alpha)
                        .uv2(light)
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

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double velocityX,
                double velocityY,
                double velocityZ
        ) {
            return new OutlinedCubeParticle(
                    level, x, y, z,
                    velocityX, velocityY, velocityZ,
                    sprites
            );
        }
    }
}
