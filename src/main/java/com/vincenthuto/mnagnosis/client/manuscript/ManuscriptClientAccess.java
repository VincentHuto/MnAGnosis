package com.vincenthuto.mnagnosis.client.manuscript;

import com.vincenthuto.mnagnosis.common.network.ManuscriptSnapshotPacket;

import net.minecraft.client.Minecraft;

public final class ManuscriptClientAccess {
    private ManuscriptClientAccess() {
    }

    public static void open(ManuscriptSnapshotPacket snapshot) {
        Minecraft.getInstance().setScreen(new LivingManuscriptScreen(snapshot));
    }
}
