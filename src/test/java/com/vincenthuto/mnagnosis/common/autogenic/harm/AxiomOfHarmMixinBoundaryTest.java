package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.vincenthuto.mnagnosis.mixin.autogenic.ComponentFireDamageHarmMixin;
import com.vincenthuto.mnagnosis.mixin.autogenic.EntityFireImmunityMixin;
import com.vincenthuto.mnagnosis.mixin.autogenic.LivingEntityPoisonImmunityMixin;
import com.vincenthuto.mnagnosis.mixin.autogenic.PotionEffectComponentHarmMixin;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AxiomOfHarmMixinBoundaryTest {

    @Test
    void fireComponentRedirectsOnlyNativeHurtCall() {
        assertRedirect(
                ComponentFireDamageHarmMixin.class,
                "ApplyEffect",
                "Lnet/minecraft/world/entity/Entity;hurt("
                        + "Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        );
    }

    @Test
    void fireImmunityRedirectsOnlyFireImmunePredicate() {
        assertRedirect(
                EntityFireImmunityMixin.class,
                "isInvulnerableTo",
                "Lnet/minecraft/world/entity/Entity;fireImmune()Z"
        );
    }

    @Test
    void potionComponentRedirectsOnlyNativeAddEffectCall() {
        assertRedirect(
                PotionEffectComponentHarmMixin.class,
                "ApplyEffect",
                "Lnet/minecraft/world/entity/LivingEntity;addEffect("
                        + "Lnet/minecraft/world/effect/MobEffectInstance;)Z"
        );
    }

    @Test
    void poisonImmunityRedirectsOnlyUndeadMobTypePredicate() {
        Redirect redirect = redirect(LivingEntityPoisonImmunityMixin.class);
        assertEquals("canBeAffected", redirect.method()[0]);
        assertEquals(1, redirect.require());
        assertEquals(
                "Lnet/minecraft/world/entity/LivingEntity;getMobType()"
                        + "Lnet/minecraft/world/entity/MobType;",
                redirect.at().target()
        );
    }

    private static void assertRedirect(
            Class<?> mixin,
            String method,
            String target
    ) {
        Redirect redirect = redirect(mixin);
        assertEquals(method, redirect.method()[0]);
        assertEquals(1, redirect.require());
        assertEquals(target, redirect.at().target());
    }

    private static Redirect redirect(Class<?> mixin) {
        for (Method method : mixin.getDeclaredMethods()) {
            Redirect redirect = method.getAnnotation(Redirect.class);
            if (redirect != null) {
                return redirect;
            }
        }
        throw new AssertionError("Expected one Redirect on " + mixin.getName());
    }
}
