package com.vincenthuto.mnagnosis.common.authorship;

import com.mna.Registries;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.authorship.part.LawInscriptionModifier;
import com.vincenthuto.mnagnosis.common.authorship.part.ComponentBanish;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredLawRegistry;
import com.vincenthuto.mnagnosis.common.authorship.law.inversion.InversionLawHandler;
import com.vincenthuto.mnagnosis.common.authorship.law.exchange.ExchangeLawHandler;
import com.vincenthuto.mnagnosis.common.authorship.law.suspension.SuspensionLawHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = MnAGnosis.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AuthorshipRegistry {

    public static final ResourceLocation LAW_INVERSION_ID = MnAGnosis.rloc("law_inversion");
    public static final ResourceLocation INVERSION_LAW_ID = MnAGnosis.rloc("inversion");
    public static final ResourceLocation BANISH_ID = MnAGnosis.rloc("components/banish");
    public static final ResourceLocation LAW_EXCHANGE_ID = MnAGnosis.rloc("law_exchange");
    public static final ResourceLocation EXCHANGE_LAW_ID = MnAGnosis.rloc("exchange");
    public static final ResourceLocation LAW_SUSPENSION_ID =
            MnAGnosis.rloc("law_suspension");
    public static final ResourceLocation SUSPENSION_LAW_ID =
            MnAGnosis.rloc("suspension");
    public static final LawInscriptionModifier LAW_INVERSION = new LawInscriptionModifier(
            MnAGnosis.rloc("textures/spell/component/true_damage.png")
    );
    public static final ComponentBanish BANISH = new ComponentBanish(
            MnAGnosis.rloc("textures/spell/component/true_damage.png")
    );
    public static final InversionLawHandler INVERSION = new InversionLawHandler();
    public static final LawInscriptionModifier LAW_EXCHANGE = new LawInscriptionModifier(
            MnAGnosis.rloc("textures/spell/component/true_damage.png")
    );
    public static final ExchangeLawHandler EXCHANGE = new ExchangeLawHandler();
    public static final LawInscriptionModifier LAW_SUSPENSION = new LawInscriptionModifier(
            MnAGnosis.rloc("textures/spell/component/true_damage.png")
    );
    public static final SuspensionLawHandler SUSPENSION = new SuspensionLawHandler();

    static {
        AuthoredLawRegistry.register(INVERSION);
        AuthoredLawRegistry.register(EXCHANGE);
        AuthoredLawRegistry.register(SUSPENSION);
    }

    private AuthorshipRegistry() {
    }

    @SubscribeEvent
    public static void registerModifiers(RegisterEvent event) {
        event.register(
                Registries.SpellEffect.get().getRegistryKey(),
                helper -> helper.register(BANISH_ID, BANISH)
        );
        event.register(
                Registries.Modifier.get().getRegistryKey(),
                helper -> {
                    helper.register(LAW_INVERSION_ID, LAW_INVERSION);
                    helper.register(LAW_EXCHANGE_ID, LAW_EXCHANGE);
                    helper.register(LAW_SUSPENSION_ID, LAW_SUSPENSION);
                }
        );
    }

    public static boolean isLawInscription(com.mna.api.spells.parts.Modifier modifier) {
        return modifier == LAW_INVERSION
                || LAW_INVERSION_ID.equals(modifier.getRegistryName())
                || modifier == LAW_EXCHANGE
                || LAW_EXCHANGE_ID.equals(modifier.getRegistryName())
                || modifier == LAW_SUSPENSION
                || LAW_SUSPENSION_ID.equals(modifier.getRegistryName());
    }

    public static Optional<ResourceLocation> lawForInscription(
            com.mna.api.spells.parts.Modifier modifier
    ) {
        if (modifier == LAW_INVERSION
                || LAW_INVERSION_ID.equals(modifier.getRegistryName())) {
            return Optional.of(INVERSION_LAW_ID);
        }
        if (modifier == LAW_EXCHANGE
                || LAW_EXCHANGE_ID.equals(modifier.getRegistryName())) {
            return Optional.of(EXCHANGE_LAW_ID);
        }
        if (modifier == LAW_SUSPENSION
                || LAW_SUSPENSION_ID.equals(modifier.getRegistryName())) {
            return Optional.of(SUSPENSION_LAW_ID);
        }
        return Optional.empty();
    }

    public static boolean isKnownAuthorship(
            ResourceLocation lawId,
            ResourceLocation interpretationId
    ) {
        return AuthoredLawRegistry.get(lawId)
                .map(handler -> handler.isKnownInterpretation(interpretationId))
                .orElse(false);
    }

    public static boolean isKnownInterpretation(ResourceLocation interpretationId) {
        return AuthoredLawRegistry.handlers().stream()
                .anyMatch(handler -> handler.isKnownInterpretation(interpretationId));
    }
}
