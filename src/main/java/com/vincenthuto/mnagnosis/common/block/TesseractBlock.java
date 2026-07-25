package com.vincenthuto.mnagnosis.common.block;

import com.vincenthuto.mnagnosis.common.block.entity.TesseractBlockEntity;
import com.vincenthuto.mnagnosis.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TesseractBlock extends BaseEntityBlock {

    public TesseractBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TesseractBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide ? createTickerHelper(type, BlockEntityRegistry.TESSERACT_BE.get(),
                TesseractBlockEntity::clientTick) : null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Use invisible render shape since we're doing custom rendering
        return RenderShape.INVISIBLE;
    }
}
