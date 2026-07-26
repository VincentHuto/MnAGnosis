package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SoundRegistry {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MnAGnosis.MODID);

    public static final RegistryObject<SoundEvent> TRUTH_GIGGLE = SOUNDS.register(
            "truth_giggle",
            () -> SoundEvent.createVariableRangeEvent(MnAGnosis.rloc("truth_giggle"))
    );
    public static final RegistryObject<SoundEvent> TRUTH_BURNING_OFFERING = SOUNDS.register(
            "truth_burning_offering",
            () -> SoundEvent.createVariableRangeEvent(MnAGnosis.rloc("truth_burning_offering"))
    );
    public static final RegistryObject<SoundEvent> TRUTH_AMBIENT = SOUNDS.register(
            "truth_ambient",
            () -> SoundEvent.createVariableRangeEvent(MnAGnosis.rloc("truth_ambient"))
    );
    public static final RegistryObject<SoundEvent> TRUTH_APPEAR = SOUNDS.register(
            "truth_appear",
            () -> SoundEvent.createVariableRangeEvent(MnAGnosis.rloc("truth_appear"))
    );
    public static final RegistryObject<SoundEvent> TRUTH_DISAPPEAR = SOUNDS.register(
            "truth_disappear",
            () -> SoundEvent.createVariableRangeEvent(MnAGnosis.rloc("truth_disappear"))
    );
    public static final RegistryObject<SoundEvent> TRUTH_VANISH = SOUNDS.register(
            "truth_vanish",
            () -> SoundEvent.createVariableRangeEvent(MnAGnosis.rloc("truth_vanish"))
    );
    public static final RegistryObject<SoundEvent> TRUE_DAMAGE_STATIC = SOUNDS.register(
            "true_damage_static",
            () -> SoundEvent.createVariableRangeEvent(MnAGnosis.rloc("true_damage_static"))
    );

    private SoundRegistry() {
    }
}
