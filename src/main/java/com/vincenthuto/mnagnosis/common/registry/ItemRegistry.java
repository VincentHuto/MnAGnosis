package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.item.armor.EnumArmorTiers;
import com.vincenthuto.mnagnosis.common.item.armor.IneffableArmorItem;
import com.vincenthuto.mnagnosis.common.item.armor.PrimalArmorItem;
import com.vincenthuto.mnagnosis.common.item.armor.TesseractItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {

    public static final DeferredRegister<Item> BASEITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            MnAGnosis.MODID);

    // HUD-only item, intentionally excluded from the creative tab.
    public static final RegistryObject<Item> INEFFABLE_HUD_BADGE = BASEITEMS.register("ineffable_hud_badge",
            () -> new Item(new Item.Properties()));

    // Base Items
    public static final RegistryObject<Item> primal_mote = BASEITEMS.register("primal_mote",
            () -> new Item(new Item.Properties()));

        public static final RegistryObject<Item> primal_crown = BASEITEMS.register("primal_crown",
                () -> new PrimalArmorItem(EnumArmorTiers.PRIMAL_CROWN, ArmorItem.Type.HELMET));
        public static final RegistryObject<Item> primal_robes= BASEITEMS.register("primal_robes",
                () -> new PrimalArmorItem(EnumArmorTiers.PRIMAL_CROWN, ArmorItem.Type.CHESTPLATE));
        public static final RegistryObject<Item> primal_legwraps = BASEITEMS.register("primal_legwraps",
                () -> new PrimalArmorItem(EnumArmorTiers.PRIMAL_CROWN, ArmorItem.Type.LEGGINGS));
        public static final RegistryObject<Item> primal_boots = BASEITEMS.register("primal_boots",
                () -> new PrimalArmorItem(EnumArmorTiers.PRIMAL_CROWN, ArmorItem.Type.BOOTS));

        public static final RegistryObject<Item> INEFFABLE_HOOD = BASEITEMS.register("ineffable_hood",
                () -> new IneffableArmorItem(ArmorItem.Type.HELMET));
        public static final RegistryObject<Item> INEFFABLE_ROBES = BASEITEMS.register("ineffable_robes",
                () -> new IneffableArmorItem(ArmorItem.Type.CHESTPLATE));
        public static final RegistryObject<Item> INEFFABLE_LEGGINGS = BASEITEMS.register("ineffable_leggings",
                () -> new IneffableArmorItem(ArmorItem.Type.LEGGINGS));
        public static final RegistryObject<Item> INEFFABLE_BOOTS = BASEITEMS.register("ineffable_boots",
                () -> new IneffableArmorItem(ArmorItem.Type.BOOTS));

        public static final RegistryObject<Item> tesseract = BASEITEMS.register("tesseract",
                () -> new TesseractItem(new Item.Properties()
                        .stacksTo(1)
                        .fireResistant()));
    public static final RegistryObject<Item> TESSERACT_BLOCK_ITEM = BASEITEMS.register("tesseract_block",
            () -> new BlockItem(BlockRegistry.TESSERACT_BLOCK.get(), new Item.Properties()));

}
