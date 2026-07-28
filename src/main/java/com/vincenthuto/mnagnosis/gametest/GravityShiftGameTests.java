package com.vincenthuto.mnagnosis.gametest;

import com.mna.Registries;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.client.gravity.GravityCameraClearance;
import com.vincenthuto.mnagnosis.common.entity.GravityShiftSurfaceEntity;
import com.vincenthuto.mnagnosis.common.network.GravityShiftStatePacket;
import com.vincenthuto.mnagnosis.common.registry.EntityRegistry;
import com.vincenthuto.mnagnosis.common.spell.SpellComponentRegistry;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityAnchorSnapshot;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityDirection;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityFrame;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityPhysics;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityMirageMath;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftApi;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftState;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravityShiftStateProvider;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravitySourceMode;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravitySupportResolver;
import com.vincenthuto.mnagnosis.common.spell.gravity.shift.GravitySurfaceMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.common.ForgeMod;
import io.netty.buffer.Unpooled;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GameTestHolder(MnAGnosis.MODID)
@PrefixGameTestTemplate(false)
public final class GravityShiftGameTests {

    private GravityShiftGameTests() {
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityFramesMapLocalDownAndRoundTrip(GameTestHelper helper) {
        Vec3 local = new Vec3(0.35D, -0.75D, 0.2D);
        for (GravityDirection gravity : GravityDirection.values()) {
            Vec3 worldDown = gravity.toWorld(new Vec3(0.0D, -1.0D, 0.0D));
            Vec3 expectedDown = Vec3.atLowerCornerOf(gravity.down().getNormal());
            helper.assertTrue(worldDown.distanceToSqr(expectedDown) < 1.0E-12D,
                    gravity + " did not map local down onto its world direction");
            Vec3 restored = gravity.toLocal(gravity.toWorld(local));
            helper.assertTrue(restored.distanceToSqr(local) < 1.0E-12D,
                    gravity + " local/world transforms were not reversible");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityLookupAndOppositesAreStable(GameTestHelper helper) {
        for (Direction direction : Direction.values()) {
            GravityDirection gravity = GravityDirection.fromDown(direction);
            helper.assertTrue(gravity.down() == direction,
                    "Direction lookup changed the requested down direction");
            helper.assertTrue(gravity.up() == direction.getOpposite(),
                    "Gravity up was not opposite gravity down");
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void orientedBoxesFollowGravityAxis(GameTestHelper helper) {
        Vec3 center = new Vec3(4.0D, 5.0D, 6.0D);
        AABB floor = GravityFrame.orientedBox(center, 0.6F, 1.8F,
                GravityDirection.DOWN);
        AABB wall = GravityFrame.orientedBox(center, 0.6F, 1.8F,
                GravityDirection.WEST);
        helper.assertTrue(close(floor.getYsize(), 1.8D)
                        && close(floor.getXsize(), 0.6D)
                        && close(floor.getZsize(), 0.6D),
                "World-down box did not retain vanilla dimensions");
        helper.assertTrue(close(wall.getXsize(), 1.8D)
                        && close(wall.getYsize(), 0.6D)
                        && close(wall.getZsize(), 0.6D),
                "Wall-gravity box did not rotate its long axis");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void directionChangeSnapsEveryInterpolatedAnchorSample(
            GameTestHelper helper
    ) {
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        entity.setOldPosAndRot();
        Vec3 wallAnchor = entity.position().add(0.7D, 0.4D, -0.2D);

        GravityAnchorSnapshot.apply(entity, wallAnchor, true);

        helper.assertTrue(
                entity.getPosition(0.0F).distanceToSqr(wallAnchor) < 1.0E-12D
                        && entity.getPosition(0.5F).distanceToSqr(wallAnchor)
                        < 1.0E-12D
                        && entity.getPosition(1.0F).distanceToSqr(wallAnchor)
                        < 1.0E-12D,
                "A direction-changing snapshot interpolated through the old gravity frame"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void shiftedCameraClearanceUsesPartialCollisionShape(
            GameTestHelper helper
    ) {
        BlockPos slabPosition = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(
                slabPosition, Blocks.STONE_SLAB.defaultBlockState()
        );
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        Vec3 camera = new Vec3(
                slabPosition.getX() + 0.5D,
                slabPosition.getY() + 0.8D,
                slabPosition.getZ() + 0.5D
        );

        Vec3 resolved = GravityCameraClearance.resolve(
                helper.getLevel(),
                entity,
                camera,
                GravityDirection.WEST
        );

        double slabTop = slabPosition.getY() + 0.5D;
        helper.assertTrue(
                resolved.y - GravityCameraClearance.SAFETY_RADIUS > slabTop
                        && resolved.y < slabTop
                        + GravityCameraClearance.SAFETY_RADIUS + 0.001D,
                "Shifted camera clearance treated a slab like a full block"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void shiftedCameraOnWallFaceEscapesOutward(
            GameTestHelper helper
    ) {
        BlockPos wallPosition = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.getLevel().setBlockAndUpdate(
                wallPosition, Blocks.STONE.defaultBlockState()
        );
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        Vec3 camera = new Vec3(
                wallPosition.getX(),
                wallPosition.getY() + 0.5D,
                wallPosition.getZ() + 0.5D
        );

        Vec3 resolved = GravityCameraClearance.resolve(
                helper.getLevel(),
                entity,
                camera,
                GravityDirection.EAST
        );

        helper.assertTrue(
                resolved.x + GravityCameraClearance.SAFETY_RADIUS
                        < wallPosition.getX()
                        && close(resolved.y, camera.y)
                        && close(resolved.z, camera.z),
                "First-person camera remained inside its wall support"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void ceilingTransitionKeepsFeetAnchoredToContactPlane(
            GameTestHelper helper
    ) {
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        AABB before = entity.getBoundingBox();
        var state = entity.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElseThrow();

        GravityShiftApi.resolveAnchored(
                entity, state, GravitySourceMode.MOBILE, GravityDirection.UP
        );

        AABB after = entity.getBoundingBox();
        helper.assertTrue(state.direction() == GravityDirection.UP
                        && close(entity.getY(), before.maxY)
                        && close(after.maxY, before.maxY),
                "Ceiling gravity left the entity anchored at its former floor");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void mobileExpiryReanchorsBeforeReturningToWorldDown(
            GameTestHelper helper
    ) {
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        var state = entity.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElseThrow();
        state.applyMobile(1);
        GravityShiftApi.resolveAnchored(
                entity, state, GravitySourceMode.MOBILE, GravityDirection.UP
        );
        AABB ceilingBox = entity.getBoundingBox();

        GravityShiftApi.tickAnchored(entity, state);

        AABB releasedBox = entity.getBoundingBox();
        helper.assertTrue(state.direction() == GravityDirection.DOWN
                        && close(releasedBox.minY, ceilingBox.minY),
                "Expired ceiling gravity rebuilt the entity away from its body");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void blockedGravityTransitionPreservesDirectionAndBounds(
            GameTestHelper helper
    ) {
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        AABB before = entity.getBoundingBox();
        BlockPos obstruction = helper.absolutePos(new BlockPos(0, 2, 2));
        helper.getLevel().setBlockAndUpdate(
                obstruction, Blocks.STONE.defaultBlockState()
        );
        var state = entity.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElseThrow();

        boolean changed = GravityShiftApi.tryResolveAnchored(
                entity, state, GravitySourceMode.MOBILE, GravityDirection.EAST
        );

        helper.assertTrue(!changed
                        && state.direction() == GravityDirection.DOWN
                        && entity.getBoundingBox().equals(before),
                "An obstructed gravity turn changed state or moved the entity");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void supportedWallRejectsImmediateFloorReversalAfterTransition(
            GameTestHelper helper
    ) {
        BlockPos floor = helper.absolutePos(new BlockPos(2, 1, 2));
        BlockPos wall = floor.east().above();
        helper.getLevel().setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(
                wall.above(), Blocks.STONE.defaultBlockState()
        );

        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        entity.setPos(
                wall.getX() - entity.getBbWidth() * 0.5D,
                floor.getY() + 1.0D,
                floor.getZ() + 0.5D
        );
        var state = entity.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElseThrow();
        state.applyMobile(40);
        GravityShiftApi.resolveAnchored(
                entity, state, GravitySourceMode.MOBILE, GravityDirection.EAST
        );
        entity.setDeltaMovement(0.0D, -0.4D, 0.0D);

        GravityDirection selected = GravitySupportResolver.chooseMobile(
                entity, state.direction(), state.previousDirection(), 0
        );

        helper.assertTrue(selected == GravityDirection.EAST,
                "The adjacent floor stole gravity while the wall still supported the entity");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void eyeAnchorInterpolatesWithGravityRotation(
            GameTestHelper helper
    ) {
        Vec3 localEye = new Vec3(0.0D, 1.62D, 0.0D);
        Vec3 start = GravityFrame.interpolatedOffset(
                localEye, GravityDirection.DOWN, GravityDirection.WEST, 0.0F
        );
        Vec3 middle = GravityFrame.interpolatedOffset(
                localEye, GravityDirection.DOWN, GravityDirection.WEST, 0.5F
        );
        Vec3 end = GravityFrame.interpolatedOffset(
                localEye, GravityDirection.DOWN, GravityDirection.WEST, 1.0F
        );
        helper.assertTrue(start.distanceToSqr(localEye) < 1.0E-10D
                        && end.distanceToSqr(new Vec3(1.62D, 0.0D, 0.0D))
                        < 1.0E-10D
                        && middle.distanceToSqr(start) > 0.01D
                        && middle.distanceToSqr(end) > 0.01D,
                "The eye anchor snapped instead of following the visual transition");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void mirageProjectionRemainsLockedToCameraRelativeTiles(
            GameTestHelper helper
    ) {
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0D), 16.0F / 9.0F, 0.05F, 1000.0F
        );
        Vector3f forward = new Vector3f(0.0F, 0.0F, -1.0F);
        Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f left = new Vector3f(-1.0F, 0.0F, 0.0F);
        Vec3 camera = new Vec3(12.0D, 40.0D, -7.0D);
        Vec3 tileCorner = camera.add(2.0D, 1.0D, -8.0D);

        Vector3f original = GravityMirageMath.projectToNdc(
                tileCorner, camera, forward, up, left, projection
        );
        Vec3 cameraShift = new Vec3(25.0D, -3.0D, 11.0D);
        Vector3f shifted = GravityMirageMath.projectToNdc(
                tileCorner.add(cameraShift),
                camera.add(cameraShift),
                forward,
                up,
                left,
                projection
        );
        helper.assertTrue(original != null && shifted != null
                        && original.distance(shifted) < 1.0E-5F,
                "Mirage tile projection changed with equivalent camera movement");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void mirageMaskShipsCameraStableCoreShader(
            GameTestHelper helper
    ) {
        ClassLoader resources = GravityShiftGameTests.class.getClassLoader();
        helper.assertTrue(
                resources.getResource(
                        "assets/mnagnosis/shaders/core/gravity_mirage_mask.json"
                ) != null
                        && resources.getResource(
                        "assets/mnagnosis/shaders/core/gravity_mirage_mask.vsh"
                ) != null
                        && resources.getResource(
                        "assets/mnagnosis/shaders/core/gravity_mirage_mask.fsh"
                ) != null,
                "Gravity Shift did not ship its camera-stable mask shader"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityShiftStateTransitionsPersistsAndReleasesSafely(
            GameTestHelper helper
    ) {
        GravityShiftState state = new GravityShiftState();
        state.applyMobile(40);
        state.resolve(GravitySourceMode.MOBILE, GravityDirection.NORTH);
        helper.assertTrue(state.mobileTicks() == 40
                        && state.mode() == GravitySourceMode.MOBILE
                        && state.direction() == GravityDirection.NORTH
                        && state.previousDirection() == GravityDirection.DOWN
                        && state.transitionTicks() == GravityShiftState.TRANSITION_TICKS,
                "Mobile gravity did not begin a bounded transition");

        CompoundTag saved = state.serializeNBT();
        GravityShiftState restored = new GravityShiftState();
        restored.deserializeNBT(saved);
        helper.assertTrue(restored.mobileTicks() == 40
                        && restored.direction() == GravityDirection.NORTH
                        && restored.revision() == state.revision(),
                "Gravity Shift state did not survive NBT");

        for (int tick = 0; tick < 40; tick++) {
            restored.tick();
        }
        helper.assertTrue(restored.mode() == GravitySourceMode.NONE
                        && restored.direction() == GravityDirection.DOWN
                        && restored.releaseGraceTicks()
                        == GravityShiftState.RELEASE_GRACE_TICKS,
                "Expired mobile gravity did not release safely to world-down");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void clientPredictionDoesNotAdvanceServerRevision(
            GameTestHelper helper
    ) {
        GravityShiftState state = new GravityShiftState();
        state.applySnapshot(
                GravitySourceMode.MOBILE,
                GravityDirection.DOWN,
                GravityDirection.NORTH,
                4,
                0,
                12L,
                2
        );
        state.tickClient();
        state.tickClient();
        helper.assertTrue(state.revision() == 12L
                        && state.mobileTicks() == 0
                        && state.mode() == GravitySourceMode.MOBILE,
                "Client interpolation mutated authoritative state or revision");

        state.applySnapshot(
                GravitySourceMode.NONE,
                GravityDirection.NORTH,
                GravityDirection.DOWN,
                GravityShiftState.TRANSITION_TICKS,
                GravityShiftState.RELEASE_GRACE_TICKS,
                13L,
                0
        );
        helper.assertTrue(state.revision() == 13L
                        && state.direction() == GravityDirection.DOWN,
                "A newer server snapshot was rejected after client prediction");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravitySurfaceFootprintIsPlanarCircularAndInward(
            GameTestHelper helper
    ) {
        BlockPos anchor = new BlockPos(10, 20, 30);
        Set<BlockPos> positions = GravitySurfaceMath.collectPlanar(
                anchor, Direction.EAST, 2.0F, ignored -> true
        );
        helper.assertTrue(positions.contains(anchor)
                        && positions.contains(anchor.offset(0, 2, 0))
                        && positions.contains(anchor.offset(0, 0, -2))
                        && !positions.contains(anchor.offset(0, 2, 2))
                        && positions.stream().allMatch(pos -> pos.getX() == anchor.getX()),
                "Surface footprint was not a circular patch on the hit plane");
        helper.assertTrue(GravitySurfaceMath.gravityForFace(Direction.EAST)
                        == GravityDirection.WEST,
                "A struck face did not pull inward toward the surface");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityShiftComponentRegistersItsContract(GameTestHelper helper) {
        SpellEffect registered = Registries.SpellEffect.get()
                .getValue(SpellComponentRegistry.GRAVITY_SHIFT_ID);
        helper.assertTrue(registered == SpellComponentRegistry.GRAVITY_SHIFT,
                "Gravity Shift was not registered in M&A's component registry");
        helper.assertTrue(registered.targetsBlocks() && registered.targetsEntities(),
                "Gravity Shift did not accept block and living-entity shapes");
        helper.assertTrue(registered.getModifiableAttributes().stream()
                        .map(pair -> pair.getAttribute())
                        .collect(Collectors.toSet())
                        .equals(Set.of(Attribute.RADIUS, Attribute.DURATION,
                                Attribute.DELAY)),
                "Gravity Shift did not expose Radius, Duration, and built-in Delay");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityShiftCastsCreateSurfaceAndMobileAdhesion(
            GameTestHelper helper
    ) {
        Zombie caster = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 1);
        ModifiedSpellPart<SpellEffect> part =
                new ModifiedSpellPart<>(SpellComponentRegistry.GRAVITY_SHIFT);
        SpellSource source = new SpellSource(caster, InteractionHand.MAIN_HAND);
        SpellContext context =
                new SpellContext(helper.getLevel(), ISpellDefinition.EMPTY);
        BlockPos anchor = helper.absolutePos(new BlockPos(2, 1, 1));

        ComponentApplicationResult blockResult =
                SpellComponentRegistry.GRAVITY_SHIFT.ApplyEffect(
                        source,
                        new SpellTarget(anchor, Direction.UP),
                        part,
                        context
                );
        java.util.List<GravityShiftSurfaceEntity> surfaces =
                helper.getLevel().getEntitiesOfClass(
                        GravityShiftSurfaceEntity.class,
                        new AABB(anchor).inflate(2.0D)
                );
        helper.assertTrue(blockResult == ComponentApplicationResult.SUCCESS
                        && surfaces.size() == 1
                        && surfaces.get(0).getAnchor().equals(anchor)
                        && surfaces.get(0).getFace() == Direction.UP,
                "Block casting did not create the authored gravity surface");

        ComponentApplicationResult selfResult =
                SpellComponentRegistry.GRAVITY_SHIFT.ApplyEffect(
                        source, new SpellTarget(caster), part, context
                );
        boolean hasMobile = caster.getCapability(
                        com.vincenthuto.mnagnosis.common.spell.gravity.shift
                                .GravityShiftStateProvider.CAPABILITY
                ).map(state -> state.mobileTicks() > 0).orElse(false);
        helper.assertTrue(selfResult == ComponentApplicationResult.SUCCESS
                        && hasMobile,
                "Self casting did not grant mobile surface adhesion");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityShiftSurfaceEntityPersistsAuthoredValues(
            GameTestHelper helper
    ) {
        GravityShiftSurfaceEntity surface = new GravityShiftSurfaceEntity(
                EntityRegistry.GRAVITY_SHIFT_SURFACE.get(), helper.getLevel()
        );
        UUID owner = UUID.randomUUID();
        BlockPos anchor = helper.absolutePos(new BlockPos(2, 2, 2));
        surface.configure(owner, anchor, Direction.NORTH, 7.0F, 240);
        CompoundTag saved = new CompoundTag();
        surface.saveWithoutId(saved);

        GravityShiftSurfaceEntity restored = new GravityShiftSurfaceEntity(
                EntityRegistry.GRAVITY_SHIFT_SURFACE.get(), helper.getLevel()
        );
        restored.load(saved);
        helper.assertTrue(owner.equals(restored.getOwnerId())
                        && anchor.equals(restored.getAnchor())
                        && restored.getFace() == Direction.NORTH
                        && restored.getRadius() == 7.0F
                        && restored.getRemainingTicks() == 240,
                "Gravity Shift surface lost its persisted cast values");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityShiftStatePacketRoundTrips(GameTestHelper helper) {
        GravityShiftStatePacket packet = new GravityShiftStatePacket(
                42,
                GravitySourceMode.MOBILE,
                GravityDirection.DOWN,
                GravityDirection.EAST,
                4,
                0,
                91L,
                120,
                10.25D,
                64.0D,
                -7.5D,
                0.125D,
                -0.25D,
                0.5D
        );
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        GravityShiftStatePacket.encode(packet, buffer);
        GravityShiftStatePacket decoded = GravityShiftStatePacket.decode(buffer);
        helper.assertTrue(packet.equals(decoded)
                        && close(decoded.anchorX(), 10.25D)
                        && close(decoded.anchorY(), 64.0D)
                        && close(decoded.anchorZ(), -7.5D)
                        && close(decoded.velocityX(), 0.125D)
                        && close(decoded.velocityY(), -0.25D)
                        && close(decoded.velocityZ(), 0.5D),
                "Gravity Shift state packet did not round-trip");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void gravityPhysicsUsesTheResolvedLocalFrame(GameTestHelper helper) {
        Vec3 fallingEast = GravityPhysics.applyGravity(
                new Vec3(0.0D, 0.2D, 0.0D), GravityDirection.EAST, 0.08D
        );
        Vec3 jumpingFromNorthWall = GravityPhysics.jump(
                Vec3.ZERO, GravityDirection.NORTH, 0.42D
        );
        Vec3 wallEye = GravityPhysics.eyePosition(
                new Vec3(2.0D, 3.0D, 4.0D), 1.62D, GravityDirection.WEST
        );
        helper.assertTrue(fallingEast.distanceToSqr(
                        new Vec3(0.08D, 0.2D, 0.0D)) < 1.0E-12D,
                "Gravity acceleration did not follow resolved down");
        helper.assertTrue(jumpingFromNorthWall.distanceToSqr(
                        new Vec3(0.0D, 0.0D, 0.42D)) < 1.0E-12D,
                "Jump impulse did not follow resolved up");
        helper.assertTrue(wallEye.distanceToSqr(
                        new Vec3(3.62D, 3.0D, 4.0D)) < 1.0E-12D,
                "Eye position did not follow resolved local up");
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void reducedEntityGravityDoesNotDrivePlayerUpWall(
            GameTestHelper helper
    ) {
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        var gravityAttribute = entity.getAttribute(
                ForgeMod.ENTITY_GRAVITY.get()
        );
        helper.assertTrue(gravityAttribute != null,
                "Forge entity gravity attribute was unavailable");
        gravityAttribute.setBaseValue(0.04D);
        var state = entity.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElseThrow();
        GravityShiftApi.resolveAnchored(
                entity, state, GravitySourceMode.MOBILE, GravityDirection.EAST
        );
        entity.setDeltaMovement(Vec3.ZERO);

        entity.travel(Vec3.ZERO);

        double expectedWallGravity = 0.04D * (double) 0.98F;
        helper.assertTrue(
                Math.abs(entity.getDeltaMovement().y) < 1.0E-9D
                        && Math.abs(entity.getDeltaMovement().x
                        - expectedWallGravity) < 1.0E-9D,
                "Reduced Forge gravity became upward wall velocity"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fixedSurfaceTravelDoesNotRetainWorldDownDrift(
            GameTestHelper helper
    ) {
        for (int y = 1; y <= 4; y++) {
            for (int z = 0; z <= 5; z++) {
                helper.getLevel().setBlockAndUpdate(
                        helper.absolutePos(new BlockPos(3, y, z)),
                        Blocks.STONE.defaultBlockState()
                );
            }
        }
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        var state = entity.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElseThrow();
        entity.setDeltaMovement(new Vec3(0.2D, -0.4D, 0.1D));
        GravityShiftApi.resolveAnchored(
                entity, state, GravitySourceMode.SURFACE, GravityDirection.EAST
        );

        Vec3 start = entity.position();
        boolean madeWallContact = false;
        for (int tick = 0; tick < 40; tick++) {
            entity.setDeltaMovement(
                    entity.getDeltaMovement().add(0.0D, 0.0D, 0.04D)
            );
            entity.travel(Vec3.ZERO);
            Vec3 velocity = entity.getDeltaMovement();
            madeWallContact |= entity.onGround();
            helper.assertTrue(
                    Math.abs(velocity.y) < 1.0E-9D
                            && Double.isFinite(velocity.x)
                            && Double.isFinite(velocity.z)
                            && velocity.x > 0.0D
                            && velocity.z == 0.0D
                            && entity.position().z == start.z,
                    "Idle fixed-wall grip allowed tangent drift at tick " + tick
            );
        }
        Vec3 settled = entity.position();
        for (int tick = 0; tick < 10; tick++) {
            entity.travel(Vec3.ZERO);
        }
        helper.assertTrue(
                madeWallContact
                        && entity.getDeltaMovement().z == 0.0D
                        && Math.abs(settled.z - start.z) < 0.75D
                        && entity.position().equals(settled),
                "Idle fixed-wall travel never reached a true stationary state"
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void fixedSurfaceForwardInputRejectsSidewaysCurrent(
            GameTestHelper helper
    ) {
        for (int y = 1; y <= 4; y++) {
            for (int z = 0; z <= 5; z++) {
                helper.getLevel().setBlockAndUpdate(
                        helper.absolutePos(new BlockPos(3, y, z)),
                        Blocks.STONE.defaultBlockState()
                );
            }
        }
        Zombie entity = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 2, 2, 2);
        var state = entity.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElseThrow();
        GravityDirection gravity = GravityDirection.EAST;
        GravityShiftApi.resolveAnchored(
                entity, state, GravitySourceMode.SURFACE, gravity
        );
        entity.setYRot(37.0F);
        entity.setSpeed(0.1F);

        for (int tick = 0; tick < 10; tick++) {
            entity.travel(Vec3.ZERO);
        }
        helper.assertTrue(entity.onGround(),
                "Entity did not settle against the fixed wall");

        float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
        Vec3 desiredLocal = new Vec3(
                -Mth.sin(yaw), 0.0D, Mth.cos(yaw)
        );
        Vec3 sidewaysLocal = new Vec3(
                Mth.cos(yaw), 0.0D, Mth.sin(yaw)
        );
        entity.setDeltaMovement(gravity.toWorld(
                sidewaysLocal.scale(0.08D).add(0.0D, -0.0784D, 0.0D)
        ));
        Vec3 before = entity.position();

        entity.travel(new Vec3(0.0D, 0.0D, 1.0D));

        Vec3 movedLocal = gravity.toLocal(entity.position().subtract(before));
        double sidewaysMovement = movedLocal.dot(sidewaysLocal);
        double forwardMovement = movedLocal.dot(desiredLocal);
        helper.assertTrue(
                Math.abs(sidewaysMovement) < 1.0E-7D
                        && forwardMovement > 0.0D,
                "Forward input retained a sideways surface current: side="
                        + sidewaysMovement + ", forward=" + forwardMovement
        );
        helper.succeed();
    }

    @GameTest(templateNamespace = MnAGnosis.MODID, template = "empty")
    public static void shiftedWallJumpDoesNotForcePlayerIntoCrawlingPose(
            GameTestHelper helper
    ) {
        for (int y = 1; y <= 4; y++) {
            for (int z = 0; z <= 5; z++) {
                helper.getLevel().setBlockAndUpdate(
                        helper.absolutePos(new BlockPos(3, y, z)),
                        Blocks.STONE.defaultBlockState()
                );
            }
        }
        Player player = helper.makeMockPlayer();
        player.getAbilities().flying = false;
        player.setPos(Vec3.atBottomCenterOf(
                helper.absolutePos(new BlockPos(2, 2, 2))
        ));
        helper.getLevel().addFreshEntity(player);
        var state = player.getCapability(GravityShiftStateProvider.CAPABILITY)
                .resolve().orElseThrow();
        GravityShiftApi.resolveAnchored(
                player, state, GravitySourceMode.SURFACE, GravityDirection.EAST
        );

        for (int tick = 0; tick < 10; tick++) {
            player.travel(Vec3.ZERO);
        }
        helper.assertTrue(player.onGround(),
                "Player did not settle against the shifted wall");
        AABB vanillaStanding = player.getDimensions(Pose.STANDING)
                .makeBoundingBox(player.position());
        AABB gravityStanding = GravityFrame.anchoredBox(
                player.position(),
                player.getDimensions(Pose.STANDING).width,
                player.getDimensions(Pose.STANDING).height,
                GravityDirection.EAST
        );
        helper.assertTrue(
                !helper.getLevel().noCollision(
                        player, vanillaStanding.deflate(1.0E-7D)
                ) && helper.getLevel().noCollision(
                        player, gravityStanding.deflate(1.0E-7D)
                ),
                "Pose regression setup did not distinguish vanilla and gravity boxes"
        );
        helper.assertTrue(
                poseBox(player, Pose.STANDING).equals(gravityStanding),
                "Player pose clearance still used a world-upright box"
        );

        player.jumpFromGround();
        player.setPose(Pose.SWIMMING);
        player.tick();

        helper.assertTrue(
                player.getPose() == Pose.STANDING,
                "Shifted wall jump forced pose " + player.getPose()
        );
        helper.succeed();
    }

    private static AABB poseBox(Player player, Pose pose) {
        try {
            var method = Entity.class.getDeclaredMethod(
                    "getBoundingBoxForPose", Pose.class
            );
            method.setAccessible(true);
            return (AABB) method.invoke(player, pose);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "Could not inspect the player pose box", exception
            );
        }
    }

    private static boolean close(double first, double second) {
        return Math.abs(first - second) < 1.0E-6D;
    }
}
