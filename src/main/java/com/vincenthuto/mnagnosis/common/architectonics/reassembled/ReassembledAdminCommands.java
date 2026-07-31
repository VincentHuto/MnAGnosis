package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import com.mojang.brigadier.CommandDispatcher;
import com.vincenthuto.mnagnosis.MnAGnosis;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumMap;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID)
public final class ReassembledAdminCommands {
    private ReassembledAdminCommands() {
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    static void register(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(Commands.literal("mnagnosis_reassembled")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                        .executes(context -> status(
                                context.getSource()))));
    }

    private static int status(CommandSourceStack source) {
        ReassembledSavedData data =
                ReassembledSavedData.get(source.getLevel());
        EnumMap<ReceiptStatus, Integer> counts =
                new EnumMap<>(ReceiptStatus.class);
        data.receipts().all().forEach(receipt -> counts.merge(
                receipt.status(), 1, Integer::sum));
        int total = data.receipts().all().size();
        source.sendSuccess(() -> Component.literal(
                "Reassembled Land: "
                        + total + " receipts; "
                        + counts.getOrDefault(
                        ReceiptStatus.ACTIVE, 0) + " active, "
                        + counts.getOrDefault(
                        ReceiptStatus.RETURN_DUE, 0) + " due, "
                        + counts.getOrDefault(
                        ReceiptStatus.RETURNING, 0) + " returning, "
                        + counts.getOrDefault(
                        ReceiptStatus.CONFLICTED, 0) + " conflicted; "
                        + data.journals().size()
                        + " write-ahead journals"),
                false);
        return total + data.journals().size();
    }
}
