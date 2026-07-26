package com.vincenthuto.mnagnosis.common.item.armor;

import com.vincenthuto.mnagnosis.client.render.item.EmptyModel;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public final class IneffableArmorItem extends ArmorItem {

    public IneffableArmorItem(Type type) {
        super(EnumArmorTiers.INEFFABLE, type, new Item.Properties().rarity(Rarity.EPIC));
    }

    public static boolean isPiece(ItemStack stack) {
        return stack.is(ItemRegistry.INEFFABLE_HOOD.get())
                || stack.is(ItemRegistry.INEFFABLE_ROBES.get())
                || stack.is(ItemRegistry.INEFFABLE_LEGGINGS.get())
                || stack.is(ItemRegistry.INEFFABLE_BOOTS.get());
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(
                    LivingEntity entity,
                    ItemStack stack,
                    EquipmentSlot slot,
                    HumanoidModel<?> original
            ) {
                return isPiece(stack) ? EmptyModel.lazyModel.get() : original;
            }
        });
    }
}
