package com.vincenthuto.mnagnosis.client.truth;

import com.vincenthuto.mnagnosis.common.registry.SoundRegistry;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public final class TruthGiggleSound extends AbstractTickableSoundInstance {

    public TruthGiggleSound() {
        super(SoundRegistry.TRUTH_GIGGLE.get(), SoundSource.AMBIENT, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.16F;
        this.pitch = 0.92F;
        this.relative = true;
    }

    @Override
    public void tick() {
    }

    public void finish() {
        this.stop();
    }
}

