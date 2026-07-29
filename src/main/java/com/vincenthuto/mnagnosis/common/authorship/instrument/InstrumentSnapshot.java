package com.vincenthuto.mnagnosis.common.authorship.instrument;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public final class InstrumentSnapshot {

    public static final int MAXIMUM_COMPRESSED_BYTES = 32 * 1024;

    private final ResourceLocation typeId;
    private final int schemaVersion;
    private final CompoundTag payload;

    private InstrumentSnapshot(
            ResourceLocation typeId,
            int schemaVersion,
            CompoundTag payload
    ) {
        this.typeId = typeId;
        this.schemaVersion = schemaVersion;
        this.payload = payload.copy();
    }

    public static InstrumentSnapshot create(
            ResourceLocation typeId,
            int schemaVersion,
            CompoundTag payload
    ) {
        Objects.requireNonNull(typeId, "typeId");
        Objects.requireNonNull(payload, "payload");
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("Instrument schema must be positive");
        }
        if (compressedBytes(payload) > MAXIMUM_COMPRESSED_BYTES) {
            throw new IllegalArgumentException("Instrument payload is too large");
        }
        return new InstrumentSnapshot(typeId, schemaVersion, payload);
    }

    public ResourceLocation typeId() {
        return typeId;
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public CompoundTag payload() {
        return payload.copy();
    }

    private static int compressedBytes(CompoundTag payload) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            NbtIo.writeCompressed(payload, output);
            return output.size();
        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Could not encode instrument payload", exception
            );
        }
    }
}
