package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.item.IneffableRobesItem;
import com.vincenthuto.mnagnosis.common.item.PrimalMoteItem;
import com.vincenthuto.mnagnosis.common.item.armor.TesseractItem;
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
            () -> new PrimalMoteItem(new Item.Properties()));

        public static final RegistryObject<Item> INEFFABLE_ROBES = BASEITEMS.register("ineffable_robes",
                IneffableRobesItem::new);

        public static final RegistryObject<Item> tesseract = BASEITEMS.register("tesseract",
                () -> new TesseractItem(new Item.Properties()
                        .stacksTo(1)
                        .fireResistant()));
    public static final RegistryObject<Item> TESSERACT_BLOCK_ITEM = BASEITEMS.register("tesseract_block",
            () -> new BlockItem(BlockRegistry.TESSERACT_BLOCK.get(), new Item.Properties()));

}
