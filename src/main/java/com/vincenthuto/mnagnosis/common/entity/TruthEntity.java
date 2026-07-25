package com.vincenthuto.mnagnosis.common.entity;

import com.mna.api.capabilities.IPlayerProgression;
import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.mna.items.ItemInit;
import com.vincenthuto.mnagnosis.common.progression.Tier6Progression;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

/**
 * The persistent server-side state for the Tier 6 offering encounter.
 * Rendering and animation are deliberately handled by the client renderer.
 */
public class TruthEntity extends Entity implements GeoEntity {

    public static final int FINALE_DURATION_TICKS = 100;
    public static final int IDLE_TIMEOUT_TICKS = 20 * 60 * 2;

    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(
            TruthEntity.class, EntityDataSerializers.OPTIONAL_UUID
    );
    private static final EntityDataAccessor<ItemStack> CODEX_OFFERING = SynchedEntityData.defineId(
            TruthEntity.class, EntityDataSerializers.ITEM_STACK
    );
    private static final EntityDataAccessor<ItemStack> WAND_OFFERING = SynchedEntityData.defineId(
            TruthEntity.class, EntityDataSerializers.ITEM_STACK
    );
    private static final EntityDataAccessor<Integer> FINALE_TICKS = SynchedEntityData.defineId(
            TruthEntity.class, EntityDataSerializers.INT
    );
    private static final EntityDataAccessor<Integer> IDLE_TICKS = SynchedEntityData.defineId(
            TruthEntity.class, EntityDataSerializers.INT
    );
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("animation.truth.idle");
    private static final RawAnimation FINALE_ANIMATION = RawAnimation.begin().thenPlay("animation.truth.finale");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private Vec3 clientFlamePosition;

    public TruthEntity(EntityType<? extends TruthEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER, Optional.empty());
        this.entityData.define(CODEX_OFFERING, ItemStack.EMPTY);
        this.entityData.define(WAND_OFFERING, ItemStack.EMPTY);
        this.entityData.define(FINALE_TICKS, 0);
        this.entityData.define(IDLE_TICKS, 0);
    }

    public boolean hasCodexOffering() {
        return !this.entityData.get(CODEX_OFFERING).isEmpty();
    }

    public boolean hasWandOffering() {
        return !this.entityData.get(WAND_OFFERING).isEmpty();
    }

    public boolean isFinaleActive() {
        return this.entityData.get(FINALE_TICKS) > 0;
    }

    public int getFinaleTicks() {
        return this.entityData.get(FINALE_TICKS);
    }

    /**
     * A renderer-friendly 0..1 progress value. The server stores a countdown so the entity can
     * discard itself on time; this inverts it for animations, particles, and shader layers.
     */
    public float getFinaleProgress(float partialTick) {
        if (!this.isFinaleActive()) {
            return 0.0F;
        }
        return Mth.clamp((FINALE_DURATION_TICKS - this.getFinaleTicks() + partialTick)
                / (float) FINALE_DURATION_TICKS, 0.0F, 1.0F);
    }

    public boolean shouldShowFinaleFlames() {
        float progress = this.getFinaleProgress(0.0F);
        return progress >= 0.10F && progress < 0.72F;
    }

    public boolean shouldShowGrin() {
        return this.getFinaleProgress(0.0F) >= 0.30F;
    }

    public boolean shouldShowGlitchSlices() {
        return this.getFinaleProgress(0.0F) >= 0.70F;
    }

    /** The aura shares the late scanline dissolve with Truth's body, preventing a leftover silhouette. */
    public boolean shouldDissolveAura() {
        return this.getFinaleProgress(0.0F) >= 0.70F;
    }

    public int getIdleTicks() {
        return this.entityData.get(IDLE_TICKS);
    }

    public Optional<UUID> getOwnerId() {
        return this.entityData.get(OWNER);
    }

    public void setOwner(Player player) {
        this.entityData.set(OWNER, Optional.of(player.getUUID()));
    }

    public boolean isOwner(Player player) {
        return this.getOwnerId().map(player.getUUID()::equals).orElse(false);
    }

    public ItemStack getCodexOffering() {
        return this.entityData.get(CODEX_OFFERING).copy();
    }

    public ItemStack getWandOffering() {
        return this.entityData.get(WAND_OFFERING).copy();
    }

    public void beginFinale() {
        if (this.isFinaleActive()) {
            return;
        }
        this.entityData.set(FINALE_TICKS, FINALE_DURATION_TICKS);
        if (!this.level().isClientSide) {
            this.level().playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.AMBIENT,
                    1.4F,
                    0.55F
            );
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            this.spawnClientParticles();
            return;
        }

        this.faceBoundOwner();

        if (this.isFinaleActive()) {
            int remaining = this.getFinaleTicks() - 1;
            this.entityData.set(FINALE_TICKS, remaining);
            if (remaining <= 0) {
                this.discard();
            }
            return;
        }

        int idleTicks = this.getIdleTicks() + 1;
        this.entityData.set(IDLE_TICKS, idleTicks);
        if (idleTicks >= IDLE_TIMEOUT_TICKS) {
            this.refundOfferings();
            this.discard();
        }
    }

    private void faceBoundOwner() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.getOwnerId()
                .map(serverLevel::getPlayerByUUID)
                .filter(owner -> owner.position().distanceToSqr(this.position()) > 1.0E-6D)
                .ifPresent(owner -> {
                    float yaw = calculateFacingYaw(this.position(), owner.position());
                    this.setYRot(yaw);
                    this.yRotO = yaw;
                });
    }

    public static float calculateFacingYaw(Vec3 from, Vec3 target) {
        double deltaX = target.x - from.x;
        double deltaZ = target.z - from.z;
        return (float) (Mth.atan2(deltaZ, deltaX) * Mth.RAD_TO_DEG) - 90.0F;
    }

    public static float calculateModelYRotation(float entityYaw) {
        return 180.0F - entityYaw;
    }

    public void setClientFlamePosition(Vec3 position) {
        if (this.level().isClientSide) {
            this.clientFlamePosition = position;
        }
    }

    private void spawnClientParticles() {
        if (this.tickCount % 2 != 0) {
            return;
        }

        float finaleProgress = this.getFinaleProgress(1.0F);
        double radius = 0.55D + finaleProgress * 1.15D;
        double angle = this.random.nextDouble() * Math.PI * 2.0D;
        double horizontal = radius * (0.55D + this.random.nextDouble() * 0.45D);
        double x = this.getX() + Math.cos(angle) * horizontal;
        double y = this.getY() + 0.25D + this.random.nextDouble() * (1.35D + finaleProgress * 0.75D);
        double z = this.getZ() + Math.sin(angle) * horizontal;

        this.level().addParticle(ParticleTypes.ENCHANT, x, y, z,
                (this.random.nextDouble() - 0.5D) * 0.015D,
                0.015D + finaleProgress * 0.025D,
                (this.random.nextDouble() - 0.5D) * 0.015D);

        if (this.random.nextBoolean()) {
            this.level().addParticle(ParticleTypes.WHITE_ASH, x, y, z,
                    (this.random.nextDouble() - 0.5D) * 0.02D,
                    0.01D,
                    (this.random.nextDouble() - 0.5D) * 0.02D);
        }
        if (this.isFinaleActive() && this.clientFlamePosition != null) {
            this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    this.clientFlamePosition.x + (this.random.nextDouble() - 0.5D) * 0.25D,
                    this.clientFlamePosition.y + this.random.nextDouble() * 0.32D,
                    this.clientFlamePosition.z + (this.random.nextDouble() - 0.5D) * 0.25D,
                    (this.random.nextDouble() - 0.5D) * 0.04D,
                    0.03D + finaleProgress * 0.05D,
                    (this.random.nextDouble() - 0.5D) * 0.04D);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "truth", 2, state -> {
            state.setAnimation(this.isFinaleActive() ? FINALE_ANIMATION : IDLE_ANIMATION);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (this.isFinaleActive()) {
            return InteractionResult.CONSUME;
        }
        if (!this.isOwner(player)) {
            player.sendSystemMessage(Component.translatable("entity.mnagnosis.truth.not_owner"));
            return InteractionResult.CONSUME;
        }

        // An owner checking on Truth keeps the encounter alive, including a duplicate or empty hand.
        this.entityData.set(IDLE_TICKS, 0);

        ItemStack heldStack = player.getItemInHand(hand);
        if (!isRequiredOffering(heldStack)) {
            player.sendSystemMessage(Component.translatable("entity.mnagnosis.truth.requires_offerings"));
            return InteractionResult.CONSUME;
        }

        if (!this.isOwnerStillEligible(player)) {
            player.sendSystemMessage(Component.translatable("entity.mnagnosis.truth.no_longer_eligible"));
            return InteractionResult.CONSUME;
        }

        OfferingResult result = this.storeOffering(heldStack);
        if (result == OfferingResult.DUPLICATE) {
            player.sendSystemMessage(Component.translatable(
                    heldStack.is(ItemInit.GUIDE_BOOK.get())
                            ? "entity.mnagnosis.truth.already_has_codex"
                            : "entity.mnagnosis.truth.already_has_wand"
            ));
            return InteractionResult.CONSUME;
        }
        takeOne(heldStack, player);
        this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.ALLAY_ITEM_GIVEN,
                SoundSource.AMBIENT,
                0.55F,
                1.2F
        );

        if (result == OfferingResult.COMPLETE) {
            this.completeTierSix((ServerPlayer) player);
        } else {
            player.sendSystemMessage(Component.translatable("entity.mnagnosis.truth.offering_accepted"));
        }
        return InteractionResult.CONSUME;
    }

    private boolean isOwnerStillEligible(Player player) {
        return player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .map(progression -> Tier6Progression.isEligibleForTruth(progression, player.level()))
                .orElse(false);
    }

    private void completeTierSix(ServerPlayer player) {
        IPlayerProgression progression = player.getCapability(PlayerProgressionProvider.PROGRESSION).orElse(null);
        if (progression == null || !Tier6Progression.isEligibleForTruth(progression, player.level())) {
            return;
        }

        progression.setTier(Tier6Progression.MAX_TIER, player);
        player.sendSystemMessage(Component.literal(Tier6Progression.TIER_SIX_ADVANCEMENT_MESSAGE));
        this.beginFinale();
    }

    public void refundOfferings() {
        ServerPlayer owner = this.getOwnerId()
                .map(ownerId -> this.level().getServer().getPlayerList().getPlayer(ownerId))
                .orElse(null);
        this.returnOffering(this.entityData.get(CODEX_OFFERING), owner);
        this.returnOffering(this.entityData.get(WAND_OFFERING), owner);
        this.entityData.set(CODEX_OFFERING, ItemStack.EMPTY);
        this.entityData.set(WAND_OFFERING, ItemStack.EMPTY);
    }

    public void refundAndDiscard() {
        this.refundOfferings();
        this.discard();
    }

    private void returnOffering(ItemStack stack, ServerPlayer owner) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack returned = stack.copy();
        if (owner != null && owner.addItem(returned)) {
            return;
        }
        if (owner != null) {
            owner.drop(returned, false);
        } else {
            this.spawnAtLocation(returned);
        }
    }

    public static boolean isRequiredOffering(ItemStack stack) {
        return stack.is(ItemInit.GUIDE_BOOK.get()) || stack.is(ItemInit.MANAWEAVER_WAND_ADVANCED.get());
    }

    /**
     * Stores a one-item NBT-preserving offering. Eligibility, ownership, inventory consumption,
     * and Tier advancement deliberately remain in {@link #interact(Player, InteractionHand)}.
     */
    public OfferingResult storeOffering(ItemStack stack) {
        if (!isRequiredOffering(stack)) {
            return OfferingResult.INVALID;
        }
        if (stack.is(ItemInit.GUIDE_BOOK.get())) {
            if (this.hasCodexOffering()) {
                return OfferingResult.DUPLICATE;
            }
            this.entityData.set(CODEX_OFFERING, copyOne(stack));
        } else {
            if (this.hasWandOffering()) {
                return OfferingResult.DUPLICATE;
            }
            this.entityData.set(WAND_OFFERING, copyOne(stack));
        }
        return this.hasCodexOffering() && this.hasWandOffering()
                ? OfferingResult.COMPLETE
                : OfferingResult.ACCEPTED;
    }

    public enum OfferingResult {
        INVALID,
        DUPLICATE,
        ACCEPTED,
        COMPLETE
    }

    private static ItemStack copyOne(ItemStack stack) {
        ItemStack offering = stack.copy();
        offering.setCount(1);
        return offering;
    }

    private static void takeOne(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            this.entityData.set(OWNER, Optional.of(tag.getUUID("Owner")));
        } else {
            this.entityData.set(OWNER, Optional.empty());
        }
        this.entityData.set(CODEX_OFFERING, ItemStack.of(tag.getCompound("CodexOffering")));
        this.entityData.set(WAND_OFFERING, ItemStack.of(tag.getCompound("WandOffering")));
        this.entityData.set(FINALE_TICKS, tag.getInt("FinaleTicks"));
        this.entityData.set(IDLE_TICKS, tag.getInt("IdleTicks"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.getOwnerId().ifPresent(owner -> tag.putUUID("Owner", owner));
        tag.put("CodexOffering", this.entityData.get(CODEX_OFFERING).save(new CompoundTag()));
        tag.put("WandOffering", this.entityData.get(WAND_OFFERING).save(new CompoundTag()));
        tag.putInt("FinaleTicks", this.getFinaleTicks());
        tag.putInt("IdleTicks", this.getIdleTicks());
    }
}
