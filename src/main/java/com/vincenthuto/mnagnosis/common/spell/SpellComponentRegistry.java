package com.vincenthuto.mnagnosis.common.spell;

import com.mna.Registries;
import com.vincenthuto.mnagnosis.MnAGnosis;
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
    public static final ResourceLocation POLARITY_ID = MnAGnosis.rloc("polarity");
    public static final PolarityModifier POLARITY = new PolarityModifier(
            MnAGnosis.rloc("textures/spell/modifier/polarity.png")
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
                }
        );
        event.register(
                Registries.Modifier.get().getRegistryKey(),
                helper -> helper.register(POLARITY_ID, POLARITY)
        );
    }

    public static boolean isPolarity(com.mna.api.spells.parts.Modifier modifier) {
        return modifier == POLARITY
                || POLARITY_ID.equals(modifier.getRegistryName());
    }
}
