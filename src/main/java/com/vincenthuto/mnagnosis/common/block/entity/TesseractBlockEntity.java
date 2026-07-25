package com.vincenthuto.mnagnosis.common.block.entity;

import com.vincenthuto.mnagnosis.common.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TesseractBlockEntity  extends BlockEntity {

    // Animation state
    private float rotation1 = 0;
    private float rotation2 = 0;
    private float rotationSpeed1 = 0.02f;
    private float rotationSpeed2 = 0.015f;

    // Pulsing effect
    private float pulseTime = 0;
    private float pulseSpeed = 0.05f;

    public TesseractBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityRegistry.TESSERACT_BE.get(), pos, state);
    }

    // Client-side tick for animation
    public static void clientTick(Level level, BlockPos pos, BlockState state, TesseractBlockEntity blockEntity) {
        blockEntity.rotation1 += blockEntity.rotationSpeed1;
        blockEntity.rotation2 += blockEntity.rotationSpeed2;
        blockEntity.pulseTime += blockEntity.pulseSpeed;

        // Keep angles in reasonable range
        if (blockEntity.rotation1 > Math.PI * 2) blockEntity.rotation1 -= Math.PI * 2;
        if (blockEntity.rotation2 > Math.PI * 2) blockEntity.rotation2 -= Math.PI * 2;
        if (blockEntity.pulseTime > Math.PI * 2) blockEntity.pulseTime -= Math.PI * 2;
    }

    public float getRotation1() {
        return rotation1;
    }

    public float getRotation2() {
        return rotation2;
    }

    public float getPulse() {
        return (float) (Math.sin(pulseTime) * 0.5 + 0.5); // 0 to 1
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putFloat("Rotation1", rotation1);
        tag.putFloat("Rotation2", rotation2);
        tag.putFloat("PulseTime", pulseTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        rotation1 = tag.getFloat("Rotation1");
        rotation2 = tag.getFloat("Rotation2");
        pulseTime = tag.getFloat("PulseTime");
    }
}
