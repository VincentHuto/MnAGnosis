package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.block.MengerianTopologyBlock;
import com.vincenthuto.mnagnosis.common.block.TesseractBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {
    public static final DeferredRegister<Block> BASEBLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            MnAGnosis.MODID);

    public static final RegistryObject<Block> TESSERACT_BLOCK = BASEBLOCKS.register("tesseract_block",
            () -> new TesseractBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.GLASS)
                    .lightLevel(state -> 10) // Emits light
                    .noOcclusion()));

    public static final RegistryObject<Block> MENGERIAN_TOPOLOGY =
            BASEBLOCKS.register(
                    "mengerian_topology",
                    () -> new MengerianTopologyBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(4.0F, 8.0F)
                                    .sound(SoundType.AMETHYST)
                                    .lightLevel(state -> 8)
                                    .noOcclusion()
                    )
            );
}
