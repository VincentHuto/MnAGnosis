package com.vincenthuto.mnagnosis.client.authorship;

import com.vincenthuto.mnagnosis.common.network.AuthorshipStatePacket;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.UUID;

public final class ClientAuthorshipState {

    private static volatile Snapshot current = Snapshot.EMPTY;

    private ClientAuthorshipState() {
    }

    public static Snapshot current() {
        return current;
    }

    public static void update(AuthorshipStatePacket packet) {
        current = new Snapshot(
                packet.mana(),
                packet.maximum(),
                packet.paradox(),
                packet.fingerprint(),
                packet.interpretations(),
                packet.selectedInterpretation(),
                packet.debts(),
                packet.declaredClosure()
        );
    }

    public static void reset() {
        current = Snapshot.EMPTY;
    }

    public record Snapshot(
            float mana,
            float maximum,
            float paradox,
            String fingerprint,
            List<ResourceLocation> interpretations,
            ResourceLocation selectedInterpretation,
            List<AuthorshipStatePacket.Debt> debts,
            UUID declaredClosure
    ) {
        private static final Snapshot EMPTY = new Snapshot(
                0.0F, 0.0F, 0.0F, "", List.of(), null, List.of(), null
        );

        public Snapshot {
            interpretations = List.copyOf(interpretations);
            debts = List.copyOf(debts);
        }
    }
}
