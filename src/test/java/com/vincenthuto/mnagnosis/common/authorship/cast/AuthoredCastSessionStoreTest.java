package com.vincenthuto.mnagnosis.common.authorship.cast;

import com.vincenthuto.mnagnosis.common.authorship.instrument.InstrumentSnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthoredCastSessionStoreTest {

    private static final UUID CASTER = UUID.fromString(
            "00000000-0000-0000-0000-000000000101"
    );

    @Test
    void newPreparationReplacesTheCastersPreviousSession() {
        AuthoredCastSessionStore<String> sessions = new AuthoredCastSessionStore<>();
        AuthoredCastSession<String> first =
                sessions.prepare(CASTER, "first", 100L, "one");
        AuthoredCastSession<String> second =
                sessions.prepare(CASTER, "second", 101L, "two");

        assertNotEquals(first.castId(), second.castId());
        assertTrue(sessions.current(CASTER, "first", 101L).isEmpty());
        assertEquals(
                "two",
                sessions.current(CASTER, "second", 101L)
                        .orElseThrow().prepared()
        );
    }

    @Test
    void sessionExpiresFortyTicksAfterPreparation() {
        AuthoredCastSessionStore<String> sessions = new AuthoredCastSessionStore<>();
        sessions.prepare(CASTER, "spell", 100L, "prepared");

        assertTrue(sessions.current(CASTER, "spell", 139L).isPresent());
        assertTrue(sessions.current(CASTER, "spell", 140L).isEmpty());
    }

    @Test
    void takingSessionIsFingerprintBoundAndSingleUse() {
        AuthoredCastSessionStore<String> sessions = new AuthoredCastSessionStore<>();
        sessions.prepare(CASTER, "spell", 100L, "prepared");

        assertTrue(sessions.take(CASTER, "other", 101L).isEmpty());
        assertEquals(
                "prepared",
                sessions.take(CASTER, "spell", 101L).orElseThrow().prepared()
        );
        assertTrue(sessions.take(CASTER, "spell", 101L).isEmpty());
    }

    @Test
    void bindsOneImmutableInstrumentSnapshotToTheMatchingCast() {
        AuthoredCastSessionStore<String> sessions = new AuthoredCastSessionStore<>();
        AuthoredCastSession<String> prepared =
                sessions.prepare(CASTER, "spell", 100L, "prepared");
        CompoundTag payload = new CompoundTag();
        payload.putString("selection", "plane");
        InstrumentSnapshot snapshot = InstrumentSnapshot.create(
                new ResourceLocation("mnagnosis", "unbounded_lattice"),
                1,
                payload
        );

        assertTrue(sessions.bindInstrument(
                CASTER, prepared.castId(), "spell", 101L, snapshot
        ));
        assertEquals(
                "plane",
                sessions.current(CASTER, "spell", 101L)
                        .orElseThrow()
                        .instrument()
                        .orElseThrow()
                        .payload()
                        .getString("selection")
        );
        assertTrue(!sessions.bindInstrument(
                CASTER, UUID.randomUUID(), "spell", 101L, snapshot
        ));
    }
}
