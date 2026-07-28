package com.vincenthuto.mnagnosis.client.render.armor;

import com.vincenthuto.mnagnosis.common.item.IneffableRobesSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.List;
import java.util.function.IntPredicate;

public final class IneffableRobesCurioLookup {

    private IneffableRobesCurioLookup() {
    }

    public static boolean isEquipped(LivingEntity entity, Item robe) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .flatMap(handler -> handler.getStacksHandler(IneffableRobesSlot.BODY))
                .map(handler -> containsVisibleMatch(
                        handler.getStacks().getSlots(),
                        index -> handler.getStacks().getStackInSlot(index).is(robe),
                        handler.getRenders()
                ))
                .orElse(false);
    }

    static boolean containsVisibleMatch(
            int slotCount,
            IntPredicate matches,
            List<Boolean> renders
    ) {
        int checkedSlots = Math.min(slotCount, renders.size());
        for (int index = 0; index < checkedSlots; index++) {
            if (Boolean.TRUE.equals(renders.get(index)) && matches.test(index)) {
                return true;
            }
        }
        return false;
    }
}
