package com.vincenthuto.mnagnosis.common.item;

import java.util.List;

import com.mna.capabilities.playerdata.progression.PlayerProgressionProvider;
import com.vincenthuto.mnagnosis.common.faction.IneffableFactionRegistry;
import com.vincenthuto.mnagnosis.common.network.NetworkHandler;
import com.vincenthuto.mnagnosis.common.progression.manuscript.ManuscriptStateProvider;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class LivingManuscriptItem extends Item {
    public LivingManuscriptItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        var progression = player.getCapability(PlayerProgressionProvider.PROGRESSION)
                .resolve().orElse(null);
        var manuscript = player.getCapability(ManuscriptStateProvider.CAPABILITY)
                .resolve().orElse(null);
        boolean eligible = progression != null
                && LivingManuscriptAccess.canOpen(
                        progression.getTier(),
                        progression.getAlliedFaction()
                                == IneffableFactionRegistry.INEFFABLE_FACTION,
                        manuscript != null);
        if (!eligible || !(player instanceof ServerPlayer serverPlayer)) {
            player.sendSystemMessage(Component.translatable(
                    "message.mnagnosis.manuscript.ineligible"));
            return InteractionResultHolder.fail(stack);
        }

        NetworkHandler.openManuscript(serverPlayer, manuscript);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("item.mnagnosis.living_manuscript.desc")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

}
