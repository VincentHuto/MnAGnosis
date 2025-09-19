package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.item.armor.EnumArmorTiers;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {

    public static final DeferredRegister<Item> BASEITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            MnAGnosis.MODID);

    // Base Items
    public static final RegistryObject<Item> primal_mote = BASEITEMS.register("primal_mote",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> primal_helm = BASEITEMS.register("primal_helm",
            () -> new ArmorItem(EnumArmorTiers.PRIMAL, ArmorItem.Type.HELMET,
                    (new Item.Properties()).fireResistant()));
    public static final RegistryObject<Item> primal_chestplate = BASEITEMS.register("primal_chestplate",
            () -> new ArmorItem(EnumArmorTiers.PRIMAL, ArmorItem.Type.CHESTPLATE,
                    (new Item.Properties()).fireResistant()));
    public static final RegistryObject<Item> primal_leggings = BASEITEMS.register("primal_leggings",
            () -> new ArmorItem(EnumArmorTiers.PRIMAL, ArmorItem.Type.LEGGINGS,
                    (new Item.Properties()).fireResistant()));
    public static final RegistryObject<Item> primal_boots = BASEITEMS.register("primal_boots",
            () -> new ArmorItem(EnumArmorTiers.PRIMAL, ArmorItem.Type.BOOTS,
                    (new Item.Properties()).fireResistant()));


}
