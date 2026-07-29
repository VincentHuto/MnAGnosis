package com.vincenthuto.mnagnosis.common.block.entity;

import com.vincenthuto.mnagnosis.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class MengerianTopologyBlockEntity extends BlockEntity {

    public MengerianTopologyBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.MENGERIAN_TOPOLOGY_BE.get(), pos, state);
    }
}
