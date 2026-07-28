package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class GravityShiftStateProvider
        implements ICapabilitySerializable<CompoundTag> {

    public static final Capability<IGravityShiftState> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private final GravityShiftState state = new GravityShiftState();
    private final LazyOptional<IGravityShiftState> optional =
            LazyOptional.of(() -> state);

    @SubscribeEvent
    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(IGravityShiftState.class);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        return capability == CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return state.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        state.deserializeNBT(tag);
    }

    public void invalidate() {
        optional.invalidate();
    }
}
