package com.vincenthuto.mnagnosis.client.authorship;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class AuthorshipKeyMappings {

    public static final KeyMapping AUTHORSHIP = new KeyMapping(
            "key.mnagnosis.authorship",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.mnagnosis"
    );

    private AuthorshipKeyMappings() {
    }
}
