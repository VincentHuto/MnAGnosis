package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.block.entity.MengerianTopologyBlockEntity;
import com.vincenthuto.mnagnosis.common.block.entity.TesseractBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, MnAGnosis.MODID);

    public static final RegistryObject< BlockEntityType<TesseractBlockEntity>> TESSERACT_BE =
            BLOCK_ENTITIES.register("tesseract_block_entity",
                    () -> BlockEntityType.Builder.of(
                            TesseractBlockEntity::new,
                            BlockRegistry.TESSERACT_BLOCK.get()
                    ).build(null));

    public static final RegistryObject<
            BlockEntityType<MengerianTopologyBlockEntity>
            > MENGERIAN_TOPOLOGY_BE = BLOCK_ENTITIES.register(
            "mengerian_topology",
            () -> BlockEntityType.Builder.of(
                    MengerianTopologyBlockEntity::new,
                    BlockRegistry.MENGERIAN_TOPOLOGY.get()
            ).build(null)
    );
}
