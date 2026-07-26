package com.vincenthuto.mnagnosis.common.authorship.law.suspension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class SuspensionSavedData extends SavedData {

    public static final String NAME = "mnagnosis_suspensions";
    private static final Comparator<SuspendedAction> ORDER =
            Comparator.comparingLong(SuspendedAction::dueGameTime)
                    .thenComparing(action -> action.contradictionId().toString());
    private final ArrayList<SuspendedAction> actions = new ArrayList<>();

    public void schedule(SuspendedAction action) {
        remove(action.contradictionId());
        actions.add(action);
        actions.sort(ORDER);
        setDirty();
    }

    public boolean remove(UUID contradictionId) {
        boolean removed = actions.removeIf(
                action -> action.contradictionId().equals(contradictionId)
        );
        if (removed) {
            setDirty();
        }
        return removed;
    }

    public List<SuspendedAction> due(long gameTime) {
        ArrayList<SuspendedAction> due = new ArrayList<>();
        while (!actions.isEmpty() && actions.get(0).dueGameTime() <= gameTime) {
            due.add(actions.remove(0));
        }
        if (!due.isEmpty()) {
            setDirty();
        }
        return List.copyOf(due);
    }

    public List<SuspendedAction> actions() {
        return List.copyOf(actions);
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        ListTag list = new ListTag();
        actions.forEach(action -> list.add(action.save()));
        root.put("actions", list);
        return root;
    }

    public static SuspensionSavedData load(CompoundTag root) {
        SuspensionSavedData data = new SuspensionSavedData();
        ListTag list = root.getList("actions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            try {
                data.actions.add(SuspendedAction.load(list.getCompound(i)));
            } catch (IllegalArgumentException ignored) {
                // Invalid versions are deliberately discarded during recovery.
            }
        }
        data.actions.sort(ORDER);
        return data;
    }
}
