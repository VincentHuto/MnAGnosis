package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID)
public final class ReassembledProtectionEvents {
    private ReassembledProtectionEvents() {
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        cancelAt(event, event.getPos());
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        cancelAt(event, event.getPos());
    }

    @SubscribeEvent
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        cancelAt(event, event.getPos());
    }

    @SubscribeEvent
    public static void onPiston(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        boolean protectedMove =
                ReassembledProtectionPolicy.rejectPiston(
                        event.getStructureHelper().getToPush(),
                        event.getStructureHelper().getToDestroy(),
                        event.getPistonMoveType()
                                == PistonEvent.PistonMoveType.RETRACT
                                ? event.getDirection().getOpposite()
                                : event.getDirection(),
                        pos -> ReassembledTransactionService.isProtected(
                                level, pos));
        if (protectedMove) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        event.getAffectedBlocks().removeIf(pos ->
                ReassembledTransactionService.isProtected(level, pos));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END
                && event.level instanceof ServerLevel level) {
            ReassembledTransactionService.tick(level);
        }
    }

    private static void cancelAt(
            BlockEvent event,
            BlockPos pos
    ) {
        if (event.getLevel() instanceof ServerLevel level
                && ReassembledTransactionService.isProtected(level, pos)) {
            event.setCanceled(true);
        }
    }
}
