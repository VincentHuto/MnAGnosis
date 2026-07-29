package com.vincenthuto.mnagnosis.common.authorship.state;

import net.minecraft.nbt.CompoundTag;

public interface ContradictionPayloadCodec<T> {
    CompoundTag encode(T value);

    T decode(CompoundTag tag);
}
