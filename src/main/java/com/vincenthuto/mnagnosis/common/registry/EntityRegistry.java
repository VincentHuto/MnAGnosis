package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import com.vincenthuto.mnagnosis.common.entity.GravityFieldEntity;
import com.vincenthuto.mnagnosis.common.entity.GravityRuptureEntity;
import com.vincenthuto.mnagnosis.common.entity.GravityShiftSurfaceEntity;
import com.vincenthuto.mnagnosis.common.entity.LivingLandControllerEntity;
import com.vincenthuto.mnagnosis.common.entity.LivingLandStrikeEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothMoonEntity;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothSunEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class EntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MnAGnosis.MODID);

    public static final RegistryObject<EntityType<TruthEntity>> TRUTH = ENTITIES.register(
            "truth",
            () -> EntityType.Builder.of(TruthEntity::new, MobCategory.MISC)
                    .sized(0.8F, 1.25F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build(MnAGnosis.rloc("truth").toString())
    );

    public static final RegistryObject<EntityType<GravityFieldEntity>> GRAVITY_FIELD =
            ENTITIES.register(
                    "gravity_field",
                    () -> EntityType.Builder
                            .<GravityFieldEntity>of(GravityFieldEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(16)
                            .updateInterval(1)
                            .build(MnAGnosis.rloc("gravity_field").toString())
            );

    public static final RegistryObject<EntityType<GravityRuptureEntity>>
            GRAVITY_RUPTURE = ENTITIES.register(
                    "gravity_rupture",
                    () -> EntityType.Builder
                            .<GravityRuptureEntity>of(
                                    GravityRuptureEntity::new, MobCategory.MISC
                            )
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(24)
                            .updateInterval(1)
                            .build(MnAGnosis.rloc("gravity_rupture").toString())
            );

    public static final RegistryObject<EntityType<GravityShiftSurfaceEntity>>
            GRAVITY_SHIFT_SURFACE = ENTITIES.register(
                    "gravity_shift_surface",
                    () -> EntityType.Builder
                            .<GravityShiftSurfaceEntity>of(
                                    GravityShiftSurfaceEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(24)
                            .updateInterval(1)
                            .build(MnAGnosis.rloc("gravity_shift_surface").toString())
            );

    public static final RegistryObject<EntityType<LivingLandStrikeEntity>> LIVING_LAND_STRIKE =
            ENTITIES.register("living_land_strike", () -> EntityType.Builder
                    .<LivingLandStrikeEntity>of(LivingLandStrikeEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.8F).clientTrackingRange(16).updateInterval(1)
                    .build(MnAGnosis.rloc("living_land_strike").toString()));

    public static final RegistryObject<EntityType<LivingLandControllerEntity>>
            LIVING_LAND_CONTROLLER = ENTITIES.register(
                    "living_land_controller", () -> EntityType.Builder
                            .<LivingLandControllerEntity>of(
                                    LivingLandControllerEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F).clientTrackingRange(16).updateInterval(1)
                            .build(MnAGnosis.rloc("living_land_controller").toString()));

    public static final RegistryObject<EntityType<YaldabaothEntity>> YALDABAOTH =
            ENTITIES.register(
                    "yaldabaoth",
                    () -> EntityType.Builder
                            .<YaldabaothEntity>of(
                                    YaldabaothEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(4.5F, 4.5F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build(MnAGnosis.rloc("yaldabaoth").toString())
            );

    public static final RegistryObject<EntityType<YaldabaothSunEntity>>
            YALDABAOTH_SUN = ENTITIES.register(
                    "yaldabaoth_sun",
                    () -> EntityType.Builder
                            .<YaldabaothSunEntity>of(
                                    YaldabaothSunEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(3.5F, 3.5F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build(MnAGnosis.rloc("yaldabaoth_sun").toString())
            );

    public static final RegistryObject<EntityType<YaldabaothMoonEntity>>
            YALDABAOTH_MOON = ENTITIES.register(
                    "yaldabaoth_moon",
                    () -> EntityType.Builder
                            .<YaldabaothMoonEntity>of(
                                    YaldabaothMoonEntity::new,
                                    MobCategory.MISC
                            )
                            .sized(3.5F, 3.5F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build(MnAGnosis.rloc("yaldabaoth_moon").toString())
            );

    private EntityRegistry() {
    }
}
