package com.vincenthuto.mnagnosis.client.truth;

import com.vincenthuto.mnagnosis.common.registry.SoundRegistry;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class TruthAmbientSound extends AbstractTickableSoundInstance {

    public TruthAmbientSound() {
        super(SoundRegistry.TRUTH_AMBIENT.get(), SoundSource.AMBIENT, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.28F;
        this.pitch = 1.0F;
        this.relative = true;
    }

    @Override
    public void tick() {
    }

    public void finish() {
        this.stop();
    }
}

