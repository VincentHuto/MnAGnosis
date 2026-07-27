package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import com.vincenthuto.mnagnosis.common.entity.GravityFieldEntity;
import com.vincenthuto.mnagnosis.common.entity.GravityRuptureEntity;
import com.vincenthuto.mnagnosis.common.entity.LivingLandControllerEntity;
import com.vincenthuto.mnagnosis.common.entity.LivingLandStrikeEntity;
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

    private EntityRegistry() {
    }
}
