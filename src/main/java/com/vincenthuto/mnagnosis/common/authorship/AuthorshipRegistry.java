package com.vincenthuto.mnagnosis.common.authorship;

import com.mna.Registries;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.authorship.part.LawInscriptionModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AuthorshipRegistry {

    public static final ResourceLocation LAW_INVERSION_ID = MnAGnosis.rloc("law_inversion");
    public static final ResourceLocation INVERSION_LAW_ID = MnAGnosis.rloc("inversion");
    public static final LawInscriptionModifier LAW_INVERSION = new LawInscriptionModifier(
            MnAGnosis.rloc("textures/spell/component/true_damage.png")
    );

    private AuthorshipRegistry() {
    }

    @SubscribeEvent
    public static void registerModifiers(RegisterEvent event) {
        event.register(
                Registries.Modifier.get().getRegistryKey(),
                helper -> helper.register(LAW_INVERSION_ID, LAW_INVERSION)
        );
    }

    public static boolean isLawInscription(com.mna.api.spells.parts.Modifier modifier) {
        return modifier == LAW_INVERSION
                || LAW_INVERSION_ID.equals(modifier.getRegistryName());
    }

    public static Optional<ResourceLocation> lawForInscription(
            com.mna.api.spells.parts.Modifier modifier
    ) {
        return isLawInscription(modifier)
                ? Optional.of(INVERSION_LAW_ID)
                : Optional.empty();
    }
}
