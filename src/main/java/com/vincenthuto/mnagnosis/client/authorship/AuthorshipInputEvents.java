package com.vincenthuto.mnagnosis.client.authorship;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(
        value = Dist.CLIENT,
        modid = MnAGnosis.MODID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class AuthorshipInputEvents {

    public static final int HOLD_TICKS = 8;
    private static boolean wasDown;
    private static int heldTicks;
    private static boolean wheelOpened;

    private AuthorshipInputEvents() {
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean down = minecraft.player != null
                && (minecraft.screen == null || minecraft.screen instanceof LawWheelScreen)
                && AuthorshipKeyMappings.AUTHORSHIP.isDown();
        if (down) {
            if (!wasDown) {
                heldTicks = 0;
            }
            heldTicks++;
            if (heldTicks == HOLD_TICKS && minecraft.screen == null) {
                minecraft.setScreen(new LawWheelScreen());
                wheelOpened = true;
            }
        } else if (wasDown) {
            if (!wheelOpened && heldTicks < HOLD_TICKS) {
                cycleInterpretation();
            }
            heldTicks = 0;
            wheelOpened = false;
        }
        wasDown = down;
    }

    private static void cycleInterpretation() {
        ClientAuthorshipState.Snapshot state = ClientAuthorshipState.current();
        List<ResourceLocation> interpretations = state.interpretations();
        if (state.fingerprint().isBlank() || interpretations.isEmpty()) {
            return;
        }
        int current = interpretations.indexOf(state.selectedInterpretation());
        ResourceLocation next = interpretations.get((current + 1) % interpretations.size());
        NetworkHandler.selectInterpretation(state.fingerprint(), next);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientAuthorshipState.reset();
        wasDown = false;
        heldTicks = 0;
        wheelOpened = false;
    }

    @Mod.EventBusSubscriber(
            value = Dist.CLIENT,
            modid = MnAGnosis.MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD
    )
    public static final class ModBus {

        private ModBus() {
        }

        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(AuthorshipKeyMappings.AUTHORSHIP);
        }
    }
}
