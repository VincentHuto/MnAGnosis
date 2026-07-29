package com.vincenthuto.mnagnosis.common.authorship;

import com.mna.api.events.CalculatingManaCostEvent;
import com.mna.api.events.ComponentApplyingEvent;
import com.mna.api.events.SpellCastEvent;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.autogenic.AutogenicCastRuntime;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AuthorshipEvents {

    private AuthorshipEvents() {
    }

    @SubscribeEvent
    public static void calculateManaCost(CalculatingManaCostEvent event) {
        if (event.getCaster() instanceof ServerPlayer player) {
            float authoredCost = AuthorshipCastingService.prepareManaCost(
                    player, event.getSpell(), event.getManaCost()
            );
            event.setManaCost(AutogenicCastRuntime.prepareManaCost(
                    player,
                    event.getSpell(),
                    authoredCost
            ));
        }
    }

    @SubscribeEvent
    public static void applyAuthoredComponent(ComponentApplyingEvent event) {
        if (event.getSource().getCaster() instanceof ServerPlayer player
                && AuthorshipCastingService.applyComponent(
                player,
                event.getContext().getSpell(),
                event.getSource(),
                event.getContext(),
                event.getTarget(),
                event.getComponent()
        )) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void finishCast(SpellCastEvent event) {
        if (event.getSource().getCaster() instanceof ServerPlayer player) {
            try {
                AuthorshipCastingService.finalizeCast(
                        player,
                        event.getSpell(),
                        event.getSource(),
                        event.getContext(),
                        event.getSpell().getManaCost()
                );
            } finally {
                AutogenicCastRuntime.finishCast(player);
            }
        }
    }
}
