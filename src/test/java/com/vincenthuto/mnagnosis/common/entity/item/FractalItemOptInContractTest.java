package com.vincenthuto.mnagnosis.common.entity.item;

import com.vincenthuto.mnagnosis.common.item.FractalBlockItem;
import com.vincenthuto.mnagnosis.common.item.FractalEntityItem;
import com.vincenthuto.mnagnosis.common.item.FractalItem;
import com.vincenthuto.mnagnosis.common.item.ApollonianTrapItem;
import com.vincenthuto.mnagnosis.common.item.KochianStarItem;
import com.vincenthuto.mnagnosis.common.item.MengerianTopologyItem;
import com.vincenthuto.mnagnosis.common.item.PrimalMoteItem;
import com.vincenthuto.mnagnosis.common.item.TesseractBlockItem;
import com.vincenthuto.mnagnosis.common.item.armor.TesseractItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FractalItemOptInContractTest {

    @Test
    void everyScopedFractalItemOptsIntoTheDedicatedEntity() {
        Class<?>[] fractalItems = {
                PrimalMoteItem.class,
                KochianStarItem.class,
                ApollonianTrapItem.class,
                TesseractItem.class,
                TesseractBlockItem.class,
                MengerianTopologyItem.class
        };

        for (Class<?> fractalItem : fractalItems) {
            assertTrue(
                    FractalEntityItem.class.isAssignableFrom(
                            fractalItem
                    ),
                    fractalItem.getSimpleName()
            );
        }
    }

    @Test
    void fractalItemBasesOwnTheCustomEntityHooks() throws Exception {
        assertTrue(
                FractalEntityItem.class.isAssignableFrom(
                        FractalItem.class
                )
        );
        assertTrue(
                FractalEntityItem.class.isAssignableFrom(
                        FractalBlockItem.class
                )
        );
        assertTrue(
                FractalItem.class.getDeclaredMethod(
                        "hasCustomEntity",
                        ItemStack.class
                ).getReturnType() == boolean.class
        );
        assertTrue(
                FractalBlockItem.class.getDeclaredMethod(
                        "hasCustomEntity",
                        ItemStack.class
                ).getReturnType() == boolean.class
        );
    }

    @Test
    void ordinaryItemsRemainOnTheVanillaEntityPath() {
        assertFalse(
                FractalEntityItem.class.isAssignableFrom(Item.class)
        );
    }
}
