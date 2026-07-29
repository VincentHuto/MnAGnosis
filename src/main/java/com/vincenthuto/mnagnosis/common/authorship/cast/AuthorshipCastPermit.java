package com.vincenthuto.mnagnosis.common.authorship.cast;

import com.vincenthuto.mnagnosis.common.authorship.instrument.InstrumentSnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class AuthorshipCastPermit {

    private final UUID castId;
    private final UUID casterId;
    private final String spellFingerprint;
    private final Optional<ResourceLocation> lawId;
    private final Optional<ResourceLocation> interpretationId;
    private final float baseManaCost;
    private final long capturedAt;
    private final CompoundTag payload;
    private final Optional<InstrumentSnapshot> instrument;

    private AuthorshipCastPermit(
            UUID castId,
            UUID casterId,
            String spellFingerprint,
            Optional<ResourceLocation> lawId,
            Optional<ResourceLocation> interpretationId,
            float baseManaCost,
            long capturedAt,
            CompoundTag payload,
            Optional<InstrumentSnapshot> instrument
    ) {
        this.castId = castId;
        this.casterId = casterId;
        this.spellFingerprint = spellFingerprint;
        this.lawId = lawId;
        this.interpretationId = interpretationId;
        this.baseManaCost = baseManaCost;
        this.capturedAt = capturedAt;
        this.payload = payload.copy();
        this.instrument = instrument;
    }

    public static AuthorshipCastPermit create(
            UUID castId,
            UUID casterId,
            String spellFingerprint,
            Optional<ResourceLocation> lawId,
            Optional<ResourceLocation> interpretationId,
            float baseManaCost,
            long capturedAt,
            CompoundTag payload,
            Optional<InstrumentSnapshot> instrument
    ) {
        Objects.requireNonNull(castId, "castId");
        Objects.requireNonNull(casterId, "casterId");
        Objects.requireNonNull(spellFingerprint, "spellFingerprint");
        Objects.requireNonNull(lawId, "lawId");
        Objects.requireNonNull(interpretationId, "interpretationId");
        Objects.requireNonNull(payload, "payload");
        Objects.requireNonNull(instrument, "instrument");
        if (spellFingerprint.isBlank()
                || !Float.isFinite(baseManaCost)
                || baseManaCost < 0.0F
                || lawId.isPresent() != interpretationId.isPresent()) {
            throw new IllegalArgumentException("Invalid authorship cast permit");
        }
        return new AuthorshipCastPermit(
                castId,
                casterId,
                spellFingerprint,
                lawId,
                interpretationId,
                baseManaCost,
                capturedAt,
                payload,
                instrument
        );
    }

    public UUID castId() {
        return castId;
    }

    public UUID casterId() {
        return casterId;
    }

    public String spellFingerprint() {
        return spellFingerprint;
    }

    public Optional<ResourceLocation> lawId() {
        return lawId;
    }

    public Optional<ResourceLocation> interpretationId() {
        return interpretationId;
    }

    public float baseManaCost() {
        return baseManaCost;
    }

    public long capturedAt() {
        return capturedAt;
    }

    public CompoundTag payload() {
        return payload.copy();
    }

    public Optional<InstrumentSnapshot> instrument() {
        return instrument;
    }
}
