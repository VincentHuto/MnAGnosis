package com.vincenthuto.mnagnosis.common.spell;

import com.mna.Registries;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.autogenic.harm.AxiomOfHarmModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SpellComponentRegistry {

    public static final ResourceLocation TRUE_DAMAGE_ID =
            MnAGnosis.rloc("components/true_damage");
    public static final ComponentTrueDamage TRUE_DAMAGE = new ComponentTrueDamage(
            MnAGnosis.rloc("textures/spell/component/true_damage.png")
    );
    public static final ResourceLocation GRAVITY_CONVERGENCE_ID =
            MnAGnosis.rloc("components/gravity_convergence");
    public static final ComponentGravityConvergence GRAVITY_CONVERGENCE =
            new ComponentGravityConvergence(
                    MnAGnosis.rloc("textures/spell/component/gravity_convergence.png")
            );
    public static final ResourceLocation GRAVITY_SHIFT_ID =
            MnAGnosis.rloc("components/gravity_shift");
    public static final ComponentGravityShift GRAVITY_SHIFT =
            new ComponentGravityShift(
                    MnAGnosis.rloc("textures/spell/component/gravity_shift.png")
            );
    public static final ResourceLocation LIVING_LAND_ID =
            MnAGnosis.rloc("components/living_land");
    public static final ComponentLivingLand LIVING_LAND = new ComponentLivingLand(
            MnAGnosis.rloc("textures/spell/component/living_land.png")
    );
    public static final ResourceLocation POLARITY_ID = MnAGnosis.rloc("polarity");
    public static final PolarityModifier POLARITY = new PolarityModifier(
            MnAGnosis.rloc("textures/spell/modifier/polarity.png")
    );
    public static final ResourceLocation PRECISION_ID = MnAGnosis.rloc("precision");
    public static final PrecisionModifier PRECISION = new PrecisionModifier(
            MnAGnosis.rloc("textures/spell/modifier/precision.png")
    );
    public static final ResourceLocation AXIOM_OF_HARM_ID =
            MnAGnosis.rloc("axiom_of_harm");
    public static final AxiomOfHarmModifier AXIOM_OF_HARM =
            new AxiomOfHarmModifier(
                    MnAGnosis.rloc(
                            "textures/spell/modifier/axiom_of_harm.png"
                    )
            );

    private SpellComponentRegistry() {
    }

    @SubscribeEvent
    public static void registerComponents(RegisterEvent event) {
        event.register(
                Registries.SpellEffect.get().getRegistryKey(),
                helper -> {
                    helper.register(TRUE_DAMAGE_ID, TRUE_DAMAGE);
                    helper.register(GRAVITY_CONVERGENCE_ID, GRAVITY_CONVERGENCE);
                    helper.register(GRAVITY_SHIFT_ID, GRAVITY_SHIFT);
                    helper.register(LIVING_LAND_ID, LIVING_LAND);
                }
        );
        event.register(
                Registries.Modifier.get().getRegistryKey(),
                helper -> {
                    helper.register(POLARITY_ID, POLARITY);
                    helper.register(PRECISION_ID, PRECISION);
                    helper.register(AXIOM_OF_HARM_ID, AXIOM_OF_HARM);
                }
        );
    }

    public static boolean isPolarity(com.mna.api.spells.parts.Modifier modifier) {
        return modifier == POLARITY
                || POLARITY_ID.equals(modifier.getRegistryName());
    }

    public static boolean isPrecision(com.mna.api.spells.parts.Modifier modifier) {
        return modifier == PRECISION
                || PRECISION_ID.equals(modifier.getRegistryName());
    }
}
