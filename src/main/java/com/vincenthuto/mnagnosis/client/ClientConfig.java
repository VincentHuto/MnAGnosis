package com.vincenthuto.mnagnosis.client;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ANIMATE_COUNTERLAW_HUD;
    public static final ForgeConfigSpec.IntValue INEFFABLE_ARMOR_SHADER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("ineffable_authorship");
        ANIMATE_COUNTERLAW_HUD = builder
                .comment("Animate Counterlaw frame reactions. Static lattice information remains.")
                .define("animateCounterlawHud", true);
        builder.pop();

        builder.push("ineffable_armor");
        INEFFABLE_ARMOR_SHADER = builder
                .comment(
                        "Ineffable armor shader: "
                                + "0 = triangles, 1 = circle grid, "
                                + "2 = FBM, 3 = fractal flash."
                )
                .defineInRange("ineffableArmorShader", 1, 0, 3);
        builder.pop();

        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}
