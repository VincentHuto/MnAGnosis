package com.vincenthuto.mnagnosis.common.spell;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public final class TrueDamageTypes {

    public static final ResourceKey<DamageType> TRUE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, MnAGnosis.rloc("true_damage"));

    private TrueDamageTypes() {
    }
}
