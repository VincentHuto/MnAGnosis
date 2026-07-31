package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.util.Optional;

public record ReassembledMove(
        BlockPos source,
        BlockPos target,
        CompoundTag sourceState,
        CompoundTag targetState
) {
    public ReassembledMove {
        source = source.immutable();
        target = target.immutable();
        sourceState = sourceState.copy();
        targetState = targetState.copy();
    }

    @Override
    public CompoundTag sourceState() {
        return sourceState.copy();
    }

    @Override
    public CompoundTag targetState() {
        return targetState.copy();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.put("Source", NbtUtils.writeBlockPos(source));
        tag.put("Target", NbtUtils.writeBlockPos(target));
        tag.put("SourceState", sourceState.copy());
        tag.put("TargetState", targetState.copy());
        return tag;
    }

    public static Optional<ReassembledMove> load(CompoundTag tag) {
        if (!tag.contains("Source", Tag.TAG_COMPOUND)
                || !tag.contains("Target", Tag.TAG_COMPOUND)
                || !tag.contains("SourceState", Tag.TAG_COMPOUND)
                || !tag.contains("TargetState", Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return Optional.of(new ReassembledMove(
                NbtUtils.readBlockPos(tag.getCompound("Source")),
                NbtUtils.readBlockPos(tag.getCompound("Target")),
                tag.getCompound("SourceState"),
                tag.getCompound("TargetState")));
    }
}
