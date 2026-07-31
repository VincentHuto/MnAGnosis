package com.vincenthuto.mnagnosis.client.architectonics;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class LatticeKeyMappings {
    public static final KeyMapping LATTICE = new KeyMapping(
            "key.mnagnosis.lattice",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.categories.mnagnosis"
    );

    private LatticeKeyMappings() {
    }
}
