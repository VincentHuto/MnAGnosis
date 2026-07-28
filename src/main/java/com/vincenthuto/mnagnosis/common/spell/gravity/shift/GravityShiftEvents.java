package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.GravityShiftSurfaceEntity;
import com.vincenthuto.mnagnosis.common.network.NetworkHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Comparator;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class GravityShiftEvents {

    private static final ResourceLocation CAPABILITY_ID =
            MnAGnosis.rloc("gravity_shift");

    private GravityShiftEvents() {
    }

    @SubscribeEvent
    public static void attachToLiving(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof LivingEntity)) {
            return;
        }
        GravityShiftStateProvider provider = new GravityShiftStateProvider();
        event.addCapability(CAPABILITY_ID, provider);
        event.addListener(provider::invalidate);
    }

    @SubscribeEvent
    public static void livingTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickLiving(event.player);
        }
    }

    @SubscribeEvent
    public static void livingTick(
            net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event
    ) {
        if (!(event.getEntity() instanceof Player)) {
            tickLiving(event.getEntity());
        }
    }

    private static void tickLiving(LivingEntity living) {
        if (living.level().isClientSide) {
            return;
        }
        living.getCapability(GravityShiftStateProvider.CAPABILITY).ifPresent(state -> {
            long before = state.revision();
            GravityShiftApi.tickAnchored(living, state);
            boolean flyingPlayer = living instanceof Player player
                    && player.getAbilities().flying;
            GravityShiftSurfaceEntity surface =
                    !state.hasMobileAdhesion() || flyingPlayer
                            ? nearestSurface(living) : null;
            GravityFlightContactPolicy.Action flightAction =
                    flyingContactAction(living, state, surface);
            if (flightAction
                    == GravityFlightContactPolicy.Action.DISABLE_FLIGHT) {
                Player player = (Player) living;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }

            if (flightAction
                    == GravityFlightContactPolicy.Action.SUSPEND_GRAVITY) {
                state.setUnsupportedTicks(0);
                GravitySourceMode suspendedMode = state.hasMobileAdhesion()
                        ? GravitySourceMode.MOBILE
                        : GravitySourceMode.NONE;
                GravityShiftApi.resolveAnchored(
                        living, state, suspendedMode,
                        GravityDirection.DOWN
                );
            } else if (mustUseWorldGravity(living)) {
                GravityShiftApi.resolveAnchored(
                        living, state, GravitySourceMode.NONE,
                        GravityDirection.DOWN
                );
            } else if (state.hasMobileAdhesion()) {
                GravitySupportResolver.Decision decision =
                        GravitySupportResolver.chooseMobile(
                        living, state.direction(), state.previousDirection(),
                        state.transitionTicks(), state.unsupportedTicks()
                );
                state.setUnsupportedTicks(decision.unsupportedTicks());
                GravityShiftApi.resolveAnchored(
                        living, state, GravitySourceMode.MOBILE,
                        decision.direction()
                );
            } else {
                if (surface == null) {
                    GravityShiftApi.resolveAnchored(
                            living, state, GravitySourceMode.NONE,
                            GravityDirection.DOWN
                    );
                } else {
                    GravityShiftApi.resolveAnchored(
                            living, state, GravitySourceMode.SURFACE,
                            surface.gravityDirection()
                    );
                }
            }
            if (state.releaseGraceTicks() > 0) {
                living.fallDistance = 0.0F;
            }
            if (state.revision() != before) {
                NetworkHandler.syncGravityShift(living);
            }
        });
    }

    private static GravityFlightContactPolicy.Action flyingContactAction(
            LivingEntity living,
            IGravityShiftState state,
            GravityShiftSurfaceEntity surface
    ) {
        if (!(living instanceof Player player)) {
            return GravityFlightContactPolicy.Action.NONE;
        }
        GravityMoveResult move =
                living instanceof GravityCollisionAccess access
                        ? access.mnagnosis$gravityMoveResult() : null;
        boolean physicalContact = living.onGround()
                || move != null && (
                move.horizontalCollision() || move.verticalCollision()
        );
        return GravityFlightContactPolicy.decide(
                player.getAbilities().flying,
                state.hasMobileAdhesion(),
                surface != null,
                physicalContact
        );
    }

    private static boolean mustUseWorldGravity(LivingEntity living) {
        if (living.isPassenger()
                || living.isSwimming()
                || living.isInWater()
                || living.isInLava()
                || living.isFallFlying()
                || living.hasEffect(MobEffects.LEVITATION)) {
            return true;
        }
        return living instanceof Player player
                && (player.isSpectator() || player.getAbilities().flying);
    }

    private static GravityShiftSurfaceEntity nearestSurface(LivingEntity living) {
        if (!(living.level() instanceof ServerLevel level)) {
            return null;
        }
        return level.getEntitiesOfClass(
                        GravityShiftSurfaceEntity.class,
                        living.getBoundingBox().inflate(13.0D),
                        surface -> !surface.isRemoved() && surface.touches(living)
                ).stream()
                .min(Comparator
                        .comparingDouble((GravityShiftSurfaceEntity surface) ->
                                surface.distanceToSqr(living))
                        .thenComparing(
                                GravityShiftSurfaceEntity::getCreatedAt,
                                Comparator.reverseOrder()
                        ))
                .orElse(null);
    }

    @SubscribeEvent
    public static void copyOnClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            return;
        }
        Player original = event.getOriginal();
        original.reviveCaps();
        original.getCapability(GravityShiftStateProvider.CAPABILITY).ifPresent(oldState ->
                event.getEntity().getCapability(GravityShiftStateProvider.CAPABILITY)
                        .ifPresent(newState -> {
                            newState.deserializeNBT(oldState.serializeNBT());
                            newState.resetOrientation();
                        })
        );
        original.invalidateCaps();
    }

    @SubscribeEvent
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        event.getEntity().getCapability(GravityShiftStateProvider.CAPABILITY)
                .ifPresent(IGravityShiftState::resetOrientation);
        NetworkHandler.syncGravityShift(event.getEntity());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        NetworkHandler.syncGravityShift(event.getEntity());
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity living) {
            NetworkHandler.syncGravityShift(living);
        }
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent event) {
        event.getEntity().getCapability(GravityShiftStateProvider.CAPABILITY)
                .ifPresent(IGravityShiftState::release);
    }
}
