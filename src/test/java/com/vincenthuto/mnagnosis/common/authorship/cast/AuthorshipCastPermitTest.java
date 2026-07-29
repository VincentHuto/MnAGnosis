package com.vincenthuto.mnagnosis.common.authorship.cast;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorshipCastPermitTest {

    @Test
    void permitDefensivelyCopiesPersistentPayload() {
        CompoundTag payload = new CompoundTag();
        payload.putString("selection", "stitch");
        AuthorshipCastPermit permit = AuthorshipCastPermit.create(
                UUID.fromString("00000000-0000-0000-0000-000000000201"),
                UUID.fromString("00000000-0000-0000-0000-000000000202"),
                "fingerprint",
                Optional.of(id("law_adjacency")),
                Optional.of(id("stitch")),
                25.0F,
                400L,
                payload,
                Optional.empty()
        );

        payload.putString("selection", "changed");
        CompoundTag returned = permit.payload();
        returned.putString("selection", "changed_again");

        assertEquals("stitch", permit.payload().getString("selection"));
    }

    @Test
    void lawAndInterpretationMustEitherBothExistOrBothBeAbsent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AuthorshipCastPermit.create(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "fingerprint",
                        Optional.of(id("law_identity")),
                        Optional.empty(),
                        1.0F,
                        1L,
                        new CompoundTag(),
                        Optional.empty()
                )
        );
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("mnagnosis", path);
    }
}
