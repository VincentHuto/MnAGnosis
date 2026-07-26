package com.vincenthuto.mnagnosis;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue INVERSION_PARADOX_COEFFICIENT =
            BUILDER.comment("Paradox generated per point of base mana cost by Inversion.")
                    .defineInRange("inversionParadoxCoefficient", 0.35D, 0.0D, 4.0D);

    public static final ForgeConfigSpec.DoubleValue FORCED_CLOSURE_MULTIPLIER =
            BUILDER.comment("Mana surcharge multiplier for Forced Closure.")
                    .defineInRange("forcedClosureMultiplier", 1.25D, 0.0D, 4.0D);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {

    }
}
