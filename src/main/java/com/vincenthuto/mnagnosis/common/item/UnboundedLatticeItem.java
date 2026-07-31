package com.vincenthuto.mnagnosis.common.item;

import com.mna.api.spells.base.ISpellDefinition;
import com.vincenthuto.mnagnosis.common.architectonics.instrument.LatticeItemState;
import com.vincenthuto.mnagnosis.common.authorship.instrument.AuthoredInstrumentProvider;
import com.vincenthuto.mnagnosis.common.authorship.instrument.InstrumentSnapshot;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class UnboundedLatticeItem extends Item
        implements AuthoredInstrumentProvider {
    public static final ResourceLocation TYPE_ID =
            com.vincenthuto.mnagnosis.MnAGnosis.rloc("unbounded_lattice");

    public UnboundedLatticeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slot,
            boolean selected
    ) {
        if (!level.isClientSide) {
            LatticeItemState.read(stack);
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.translatable(
                    "message.mnagnosis.lattice.open_radial"));
        }
        return InteractionResultHolder.sidedSuccess(
                stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide
                && context.getPlayer() != null) {
            context.getPlayer().sendSystemMessage(Component.translatable(
                    "message.mnagnosis.lattice.open_radial"));
        }
        return InteractionResult.sidedSuccess(
                context.getLevel().isClientSide);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable(
                        "item.mnagnosis.unbounded_lattice.desc")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable(
                        "item.mnagnosis.unbounded_lattice.pattern",
                        Component.translatable(
                                "message.mnagnosis.lattice.pattern."
                                        + LatticeItemState.read(stack)
                                        .pattern().name().toLowerCase(
                                                Locale.ROOT)))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ResourceLocation typeId() {
        return TYPE_ID;
    }

    @Override
    public boolean supports(ItemStack stack) {
        return stack.getItem() instanceof UnboundedLatticeItem;
    }

    @Override
    public Optional<InstrumentSnapshot> snapshot(
            ServerPlayer player,
            ItemStack stack,
            ISpellDefinition spell
    ) {
        var lattice = LatticeItemState.read(stack);
        CompoundTag payload = new CompoundTag();
        payload.putString("Pattern", lattice.pattern().name());
        payload.putUUID("Nonce", lattice.itemNonce());
        return Optional.of(InstrumentSnapshot.create(
                TYPE_ID,
                LatticeItemState.SCHEMA_VERSION,
                payload));
    }
}
