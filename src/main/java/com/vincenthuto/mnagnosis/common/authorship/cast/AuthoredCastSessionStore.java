package com.vincenthuto.mnagnosis.common.authorship.cast;

import com.vincenthuto.mnagnosis.common.authorship.instrument.InstrumentSnapshot;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthoredCastSessionStore<T> {

    public static final int LIFETIME_TICKS = 40;

    private final Map<UUID, AuthoredCastSession<T>> sessions =
            new ConcurrentHashMap<>();

    public AuthoredCastSession<T> prepare(
            UUID casterId,
            String spellFingerprint,
            long gameTime,
            T prepared
    ) {
        AuthoredCastSession<T> session = new AuthoredCastSession<>(
                UUID.randomUUID(),
                casterId,
                spellFingerprint,
                gameTime,
                gameTime + LIFETIME_TICKS,
                prepared,
                Optional.empty()
        );
        sessions.put(casterId, session);
        return session;
    }

    public Optional<AuthoredCastSession<T>> current(
            UUID casterId,
            String spellFingerprint,
            long gameTime
    ) {
        AuthoredCastSession<T> session = sessions.get(casterId);
        if (session == null) {
            return Optional.empty();
        }
        if (gameTime >= session.expiresAt()) {
            sessions.remove(casterId, session);
            return Optional.empty();
        }
        return session.spellFingerprint().equals(spellFingerprint)
                ? Optional.of(session)
                : Optional.empty();
    }

    public Optional<AuthoredCastSession<T>> take(
            UUID casterId,
            String spellFingerprint,
            long gameTime
    ) {
        Optional<AuthoredCastSession<T>> current =
                current(casterId, spellFingerprint, gameTime);
        current.ifPresent(session -> sessions.remove(casterId, session));
        return current;
    }

    public boolean bindInstrument(
            UUID casterId,
            UUID castId,
            String spellFingerprint,
            long gameTime,
            InstrumentSnapshot instrument
    ) {
        Optional<AuthoredCastSession<T>> current =
                current(casterId, spellFingerprint, gameTime);
        if (current.isEmpty()) {
            return false;
        }
        AuthoredCastSession<T> session = current.orElseThrow();
        if (!session.castId().equals(castId) || session.instrument().isPresent()) {
            return false;
        }
        AuthoredCastSession<T> bound = new AuthoredCastSession<>(
                session.castId(),
                session.casterId(),
                session.spellFingerprint(),
                session.preparedAt(),
                session.expiresAt(),
                session.prepared(),
                Optional.of(instrument)
        );
        return sessions.replace(casterId, session, bound);
    }

    public void forget(UUID casterId) {
        sessions.remove(casterId);
    }
}
