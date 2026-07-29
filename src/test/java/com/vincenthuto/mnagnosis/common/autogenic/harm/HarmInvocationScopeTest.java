package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.vincenthuto.mnagnosis.common.authorship.cast.AuthorshipCastPermit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarmInvocationScopeTest {
    private static final UUID CASTER =
            UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final UUID TARGET =
            UUID.fromString("00000000-0000-0000-0000-000000000402");
    private static final UUID OTHER_TARGET =
            UUID.fromString("00000000-0000-0000-0000-000000000403");

    @Test
    void matchingNativeCallConsumesAuthorizationExactlyOnce() {
        Object part = new Object();
        Object context = new Object();
        Object nativeHarm = new Object();

        HarmInvocationScope scope = open(part, context, TARGET);
        boolean nativeResult = scope.invoke(
                TARGET,
                HarmGate.FIRE_TYPE_IMMUNITY,
                nativeHarm,
                () -> {
                    assertTrue(HarmInvocationScope.consume(
                            TARGET,
                            HarmGate.FIRE_TYPE_IMMUNITY,
                            nativeHarm
                    ));
                    assertFalse(HarmInvocationScope.consume(
                            TARGET,
                            HarmGate.FIRE_TYPE_IMMUNITY,
                            nativeHarm
                    ));
                    return true;
                }
        );
        scope.close();

        assertTrue(nativeResult);
        assertEquals(
                new HarmInvocationScope.Outcome(
                        true,
                        true,
                        HarmGate.FIRE_TYPE_IMMUNITY
                ),
                scope.outcome()
        );
    }

    @Test
    void differentTargetGateOrNativeObjectCannotBorrowAuthorization() {
        Object nativeHarm = new Object();
        HarmInvocationScope scope = open(new Object(), new Object(), TARGET);

        scope.invoke(
                TARGET,
                HarmGate.FIRE_TYPE_IMMUNITY,
                nativeHarm,
                () -> {
                    assertFalse(HarmInvocationScope.consume(
                            OTHER_TARGET,
                            HarmGate.FIRE_TYPE_IMMUNITY,
                            nativeHarm
                    ));
                    assertFalse(HarmInvocationScope.consume(
                            TARGET,
                            HarmGate.UNDEAD_POISON_IMMUNITY,
                            nativeHarm
                    ));
                    assertFalse(HarmInvocationScope.consume(
                            TARGET,
                            HarmGate.FIRE_TYPE_IMMUNITY,
                            new Object()
                    ));
                    return false;
                }
        );
        scope.close();

        assertEquals(
                new HarmInvocationScope.Outcome(
                        false,
                        false,
                        HarmGate.FIRE_TYPE_IMMUNITY
                ),
                scope.outcome()
        );
    }

    @Test
    void nestedOwnerShadowsOuterAuthorizationAndBothCloseCleanly() {
        Object outerNative = new Object();
        HarmInvocationScope outer = open(new Object(), new Object(), TARGET);

        outer.invoke(
                TARGET,
                HarmGate.FIRE_TYPE_IMMUNITY,
                outerNative,
                () -> {
                    HarmInvocationScope nested = open(
                            new Object(),
                            new Object(),
                            OTHER_TARGET
                    );
                    assertFalse(HarmInvocationScope.consume(
                            TARGET,
                            HarmGate.FIRE_TYPE_IMMUNITY,
                            outerNative
                    ));
                    nested.close();
                    assertTrue(HarmInvocationScope.consume(
                            TARGET,
                            HarmGate.FIRE_TYPE_IMMUNITY,
                            outerNative
                    ));
                    return true;
                }
        );
        outer.close();

        assertFalse(HarmInvocationScope.hasFrames());
    }

    @Test
    void exceptionClosesNativeFrameAndOwnerCloseClearsThreadLocal() {
        HarmInvocationScope scope = open(new Object(), new Object(), TARGET);

        assertThrows(
                IllegalStateException.class,
                () -> scope.invoke(
                        TARGET,
                        HarmGate.FIRE_TYPE_IMMUNITY,
                        new Object(),
                        () -> {
                            throw new IllegalStateException("native failure");
                        }
                )
        );
        scope.close();

        assertFalse(HarmInvocationScope.hasFrames());
        assertFalse(scope.outcome().nativeSucceeded());
    }

    @Test
    void authorizationRetainsPermitPartAndContextIdentity() {
        Object part = new Object();
        Object context = new Object();
        HarmInvocationScope scope = open(part, context, TARGET);

        assertEquals(permit().castId(), scope.authorization().castId());
        assertEquals(2, scope.authorization().componentIndex());
        assertEquals(part, scope.authorization().partIdentity());
        assertEquals(context, scope.authorization().contextIdentity());
        assertEquals(TARGET, scope.authorization().targetId());
        scope.close();
    }

    private static HarmInvocationScope open(
            Object part,
            Object context,
            UUID target
    ) {
        return HarmInvocationScope.open(
                permit(),
                2,
                FireDamageHarmAdapter.COMPONENT_ID,
                part,
                context,
                target,
                FireDamageHarmAdapter.ID,
                HarmGate.FIRE_TYPE_IMMUNITY
        );
    }

    private static AuthorshipCastPermit permit() {
        return AuthorshipCastPermit.create(
                UUID.fromString("00000000-0000-0000-0000-000000000404"),
                CASTER,
                "axiom-fingerprint",
                Optional.empty(),
                Optional.empty(),
                100.0F,
                40L,
                new CompoundTag(),
                Optional.empty()
        );
    }
}
