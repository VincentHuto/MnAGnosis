package com.vincenthuto.mnagnosis.common.authorship.cast;

import com.vincenthuto.mnagnosis.common.authorship.instrument.InstrumentSnapshot;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record AuthoredCastSession<T>(
        UUID castId,
        UUID casterId,
        String spellFingerprint,
        long preparedAt,
        long expiresAt,
        T prepared,
        Optional<InstrumentSnapshot> instrument
) {
    public AuthoredCastSession {
        Objects.requireNonNull(castId, "castId");
        Objects.requireNonNull(casterId, "casterId");
        Objects.requireNonNull(spellFingerprint, "spellFingerprint");
        Objects.requireNonNull(prepared, "prepared");
        instrument = instrument == null ? Optional.empty() : instrument;
        if (spellFingerprint.isBlank() || expiresAt <= preparedAt) {
            throw new IllegalArgumentException("Invalid authored cast session");
        }
    }
}
