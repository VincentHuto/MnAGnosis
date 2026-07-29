package com.vincenthuto.mnagnosis.client.render.armor;

import net.minecraft.client.model.geom.ModelPart;

final class IneffableArmoredPose {

    private IneffableArmoredPose() {
    }

    static void copyPart(
            ModelPart parent,
            ModelPart armored,
            float parentBaseX,
            float parentBaseY,
            float parentBaseZ,
            float armoredBaseX,
            float armoredBaseY,
            float armoredBaseZ
    ) {
        armored.x = armoredBaseX + parent.x - parentBaseX;
        armored.y = armoredBaseY + parent.y - parentBaseY;
        armored.z = armoredBaseZ + parent.z - parentBaseZ;
        armored.xRot = parent.xRot;
        armored.yRot = parent.yRot;
        armored.zRot = parent.zRot;
        armored.xScale = parent.xScale;
        armored.yScale = parent.yScale;
        armored.zScale = parent.zScale;
        armored.visible = parent.visible;
    }
}
