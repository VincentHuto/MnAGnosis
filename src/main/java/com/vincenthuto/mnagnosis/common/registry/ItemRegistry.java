package com.vincenthuto.mnagnosis.common.registry;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.item.ApollonianTrapItem;
import com.vincenthuto.mnagnosis.common.item.IneffableRobesItem;
import com.vincenthuto.mnagnosis.common.item.IneffableHudBadgeItem;
import com.vincenthuto.mnagnosis.common.item.KochianStarItem;
import com.vincenthuto.mnagnosis.common.item.MengerianTopologyItem;
import com.vincenthuto.mnagnosis.common.item.LivingManuscriptItem;
import com.vincenthuto.mnagnosis.common.item.PrimalMoteItem;
import com.vincenthuto.mnagnosis.common.item.TesseractBlockItem;
import com.vincenthuto.mnagnosis.common.item.UnboundedLatticeItem;
import com.vincenthuto.mnagnosis.common.item.armor.TesseractItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {

    public static final DeferredRegister<Item> BASEITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            MnAGnosis.MODID);

    // HUD-only item, intentionally excluded from the creative tab.
    public static final RegistryObject<Item> INEFFABLE_HUD_BADGE = BASEITEMS.register("ineffable_hud_badge",
            () -> new IneffableHudBadgeItem(new Item.Properties()));

    // Base Items
    public static final RegistryObject<Item> primal_mote = BASEITEMS.register("primal_mote",
            () -> new PrimalMoteItem(new Item.Properties()));

    public static final RegistryObject<Item> LIVING_MANUSCRIPT =
            BASEITEMS.register(
                    "living_manuscript",
                    () -> new LivingManuscriptItem(new Item.Properties().stacksTo(1))
            );

    public static final RegistryObject<Item> UNBOUNDED_LATTICE =
            BASEITEMS.register(
                    "unbounded_lattice",
                    () -> new UnboundedLatticeItem(
                            new Item.Properties().stacksTo(1).fireResistant())
            );

    public static final RegistryObject<Item> APOLLONIAN_TRAP =
            BASEITEMS.register(
                    "apollonian_trap",
                    () -> new ApollonianTrapItem(
                            new Item.Properties().stacksTo(1)
                    )
            );

    public static final RegistryObject<Item> KOCHIAN_STAR =
            BASEITEMS.register(
                    "kochian_star",
                    () -> new KochianStarItem(
                            new Item.Properties().stacksTo(1)
                    )
            );

        public static final RegistryObject<Item> INEFFABLE_ROBES = BASEITEMS.register("ineffable_robes",
                IneffableRobesItem::new);

        public static final RegistryObject<Item> tesseract = BASEITEMS.register("tesseract",
                () -> new TesseractItem(new Item.Properties()
                        .stacksTo(1)
                        .fireResistant()));
    public static final RegistryObject<Item> TESSERACT_BLOCK_ITEM = BASEITEMS.register("tesseract_block",
            () -> new TesseractBlockItem(
                    BlockRegistry.TESSERACT_BLOCK.get(),
                    new Item.Properties()
            ));

    public static final RegistryObject<Item> MENGERIAN_TOPOLOGY =
            BASEITEMS.register(
                    "mengerian_topology",
                    () -> new MengerianTopologyItem(
                            BlockRegistry.MENGERIAN_TOPOLOGY.get(),
                            new Item.Properties().fireResistant()
                    )
            );

}
