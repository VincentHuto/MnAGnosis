package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SuspensionScheduler {

    private SuspensionScheduler() {
    }

    public static void schedule(ServerLevel level, SuspendedAction action) {
        data(level).schedule(action);
    }

    public static void cancel(ServerLevel level, UUID contradictionId) {
        data(level).remove(contradictionId);
    }

    @SubscribeEvent
    public static void tick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.level instanceof ServerLevel level)) {
            return;
        }
        for (SuspendedAction action : data(level).due(level.getGameTime())) {
            if (action.dimension().equals(level.dimension())) {
                SuspensionLawHandler.releaseScheduled(
                        level, action, ReleaseReason.DUE
                );
            }
        }
    }

    static SuspensionSavedData data(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                SuspensionSavedData::load,
                SuspensionSavedData::new,
                SuspensionSavedData.NAME
        );
    }

    public enum ReleaseReason {
        DUE,
        CLOSURE,
        VENT
    }
}
