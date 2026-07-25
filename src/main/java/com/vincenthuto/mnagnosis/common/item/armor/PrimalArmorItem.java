package com.vincenthuto.mnagnosis.common.item.armor;

import com.vincenthuto.mnagnosis.client.render.item.EmptyModel;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class PrimalArmorItem extends ArmorItem {

    public PrimalArmorItem(ArmorMaterial materialIn, Type slot) {
        super(materialIn, slot, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entityLiving, ItemStack itemStack,
                                                          EquipmentSlot armorSlot, HumanoidModel<?> _default) {
                //Having them be empty here because they are rendered in the entity layer
                if (itemStack.getItem() == ItemRegistry.primal_crown.get()) {
                    return EmptyModel.lazyModel.get();
                }
                if (itemStack.getItem() == ItemRegistry.primal_robes.get()) {
                    return EmptyModel.lazyModel.get();
                }
                if (itemStack.getItem() == ItemRegistry.primal_legwraps.get()) {
                    return EmptyModel.lazyModel.get();
                }
                if (itemStack.getItem() == ItemRegistry.primal_boots.get()) {
                    return EmptyModel.lazyModel.get();
                }

                return IClientItemExtensions.super.getHumanoidArmorModel(entityLiving, itemStack, armorSlot, _default);
            }

//            @Override
//            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
//                return new MarrowCrownItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
//                        Minecraft.getInstance().getEntityModels());
//            }
        });

    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.literal(""));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotIndex, boolean p_41408_) {
        super.inventoryTick(stack, level, entity, slotIndex, p_41408_);

        //		if (world.isClientSide) {
//			for (int i = 0; i < 1; ++i) {
//				if (i % 2 == 0) {
//					world.addParticle(DustParticleOptions.REDSTONE, player.getRandomX(0.5D), player.getY(),
//							player.getRandomZ(0.5D), (world.random.nextDouble() - 0.5D) * 2.0D,
//							-world.random.nextDouble(), (world.random.nextDouble() - 0.5D) * 2.0D);
//				}
//			}
//		}
    }


}
