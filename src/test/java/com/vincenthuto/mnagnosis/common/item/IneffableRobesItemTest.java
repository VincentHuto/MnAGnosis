package com.vincenthuto.mnagnosis.common.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableRobesItemTest {

    @Test
    void acceptsOnlyTheCuriosBodySlot() {
        assertTrue(IneffableRobesSlot.isBody("body"));
        assertFalse(IneffableRobesSlot.isBody("back"));
        assertFalse(IneffableRobesSlot.isBody(""));
    }

    @Test
    void isAnOrdinaryItemInsteadOfArmor() {
        assertTrue(Item.class.isAssignableFrom(IneffableRobesItem.class));
        assertFalse(ArmorItem.class.isAssignableFrom(IneffableRobesItem.class));
    }
}
