package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.TruthEntity;
import com.vincenthuto.mnagnosis.common.entity.GravityFieldEntity;
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

    private EntityRegistry() {
    }
}
