package com.vincenthuto.mnagnosis.client.authorship;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientAuthorshipConfig {

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ANIMATE_COUNTERLAW_HUD;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("ineffable_authorship");
        ANIMATE_COUNTERLAW_HUD = builder
                .comment("Animate Counterlaw frame reactions. Static lattice information remains.")
                .define("animateCounterlawHud", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientAuthorshipConfig() {
    }
}
