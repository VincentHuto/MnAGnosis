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

    private SpellComponentRegistry() {
    }

    @SubscribeEvent
    public static void registerComponents(RegisterEvent event) {
        event.register(
                Registries.SpellEffect.get().getRegistryKey(),
                helper -> helper.register(TRUE_DAMAGE_ID, TRUE_DAMAGE)
        );
    }
}
