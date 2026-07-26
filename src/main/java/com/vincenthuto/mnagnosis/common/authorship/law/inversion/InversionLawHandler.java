package com.vincenthuto.mnagnosis.common.authorship.law.inversion;

import com.mna.Registries;
import com.mna.api.spells.ComponentApplicationResult;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;
import com.mna.api.spells.targeting.SpellContext;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.crafting.ModifiedSpellPart;
import com.vincenthuto.mnagnosis.Config;
import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipCastingService;
import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredCastContext;
import com.vincenthuto.mnagnosis.common.authorship.law.AuthoredLawHandler;
import com.vincenthuto.mnagnosis.common.authorship.state.Contradiction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class InversionLawHandler implements AuthoredLawHandler {

    public static final ResourceLocation VECTOR = MnAGnosis.rloc("vector");
    public static final ResourceLocation VITALITY = MnAGnosis.rloc("vitality");
    public static final ResourceLocation REVELATION = MnAGnosis.rloc("revelation");
    public static final ResourceLocation PRESENCE = MnAGnosis.rloc("presence");
    public static final ResourceLocation MOTION = MnAGnosis.rloc("motion");

    private static final List<InversionRelationship> RELATIONSHIPS = List.of(
            pair(VECTOR, "mna:components/fling", "mna:components/pull"),
            pair(VITALITY, "mna:components/heal", "mna:components/magic_damage"),
            pair(REVELATION, "mna:components/divination", "mna:components/invisibility"),
            new InversionRelationship(
                    PRESENCE,
                    id("mna:components/insect_swarm"),
                    AuthorshipRegistry.BANISH_ID
            ),
            pair(MOTION, "mna:components/haste", "mna:components/slow")
    );

    @Override
    public ResourceLocation lawId() {
        return AuthorshipRegistry.INVERSION_LAW_ID;
    }

    @Override
    public List<ResourceLocation> interpretations(ISpellDefinition spell) {
        Set<ResourceLocation> compatible = new LinkedHashSet<>();
        for (IModifiedSpellPart<SpellEffect> component : spell.getComponents()) {
            ResourceLocation componentId = Registries.SpellEffect.get().getKey(component.getPart());
            relationshipFor(componentId).map(InversionRelationship::interpretationId)
                    .ifPresent(compatible::add);
        }
        return List.copyOf(compatible);
    }

    @Override
    public boolean supports(ResourceLocation componentId, ResourceLocation interpretationId) {
        return relationshipFor(componentId)
                .filter(relationship -> relationship.interpretationId().equals(interpretationId))
                .isPresent();
    }

    @Override
    public float paradox(AuthoredCastContext context) {
        float result = (float) Math.ceil(
                context.baseManaCost() * Config.INVERSION_PARADOX_COEFFICIENT.get()
        );
        return Math.max(1.0F, result);
    }

    @Override
    public ComponentApplicationResult applyAuthored(
            AuthoredCastContext context,
            IModifiedSpellPart<SpellEffect> original,
            SpellTarget target
    ) {
        ResourceLocation originalId = Registries.SpellEffect.get().getKey(original.getPart());
        InversionRelationship relationship = relationshipFor(originalId)
                .filter(value -> value.interpretationId().equals(context.interpretationId()))
                .orElse(null);
        if (relationship == null) {
            return ComponentApplicationResult.FAIL;
        }
        ResourceLocation complementId = relationship.complementOf(originalId);
        SpellEffect complement = Registries.SpellEffect.get().getValue(complementId);
        if (complement == null || !isLegalTarget(context.source(), target, complement, context)) {
            return ComponentApplicationResult.FAIL;
        }

        ModifiedSpellPart<SpellEffect> replacement = new ModifiedSpellPart<>(complement);
        copySharedAttributes(original, replacement);
        ComponentApplicationResult result = complement.ApplyEffect(
                context.source(), target, replacement, context.spellContext()
        );
        if (result == ComponentApplicationResult.SUCCESS) {
            AuthorshipCastingService.setAuthoredPayload(
                    context.spellContext(),
                    payload(context, target, originalId, complementId, replacement)
            );
        }
        return result;
    }

    @Override
    public boolean isPerfectClosure(Contradiction debt, AuthoredCastContext context) {
        CompoundTag payload = debt.payload();
        ResourceLocation required = ResourceLocation.tryParse(payload.getString("original"));
        if (required == null) {
            return false;
        }
        Optional<CompoundTag> currentAuthored =
                AuthorshipCastingService.authoredPayload(context.spellContext());
        if (currentAuthored.isPresent()) {
            ResourceLocation realized = ResourceLocation.tryParse(
                    currentAuthored.orElseThrow().getString("complement")
            );
            return required.equals(realized);
        }
        return context.spell().getComponents().stream()
                .map(IModifiedSpellPart::getPart)
                .map(component -> Registries.SpellEffect.get().getKey(component))
                .anyMatch(required::equals);
    }

    @Override
    public void vent(ServerPlayer player, Contradiction debt) {
        CompoundTag payload = debt.payload();
        ResourceLocation complementId =
                ResourceLocation.tryParse(payload.getString("complement"));
        SpellEffect complement = complementId == null
                ? null : Registries.SpellEffect.get().getValue(complementId);
        if (complement == null) {
            return;
        }

        ServerLevel level = loadedLevel(player, payload).orElse(player.serverLevel());
        SpellTarget target = loadedTarget(level, payload)
                .map(SpellTarget::new)
                .orElseGet(() -> new SpellTarget(player));
        ModifiedSpellPart<SpellEffect> replacement = new ModifiedSpellPart<>(complement);
        restoreAttributes(payload.getCompound("attributes"), replacement);
        SpellContext context = new SpellContext(level, ISpellDefinition.EMPTY);
        SpellSource source = new SpellSource(player, InteractionHand.MAIN_HAND);
        if (isLegalTarget(source, target, complement, null)) {
            complement.ApplyEffect(source, target, replacement, context);
        }
    }

    public static Optional<InversionRelationship> relationshipFor(
            ResourceLocation componentId
    ) {
        if (componentId == null) {
            return Optional.empty();
        }
        return RELATIONSHIPS.stream()
                .filter(relationship -> relationship.contains(componentId))
                .findFirst();
    }

    private static void copySharedAttributes(
            IModifiedSpellPart<SpellEffect> original,
            ModifiedSpellPart<SpellEffect> replacement
    ) {
        for (Attribute attribute : original.getContainedAttributes()) {
            if (replacement.getContainedAttributes().contains(attribute)) {
                replacement.setValue(attribute, original.getValue(attribute));
                replacement.setMultiplier(attribute, original.getMultiplier(attribute));
            }
        }
    }

    private static boolean isLegalTarget(
            SpellSource source,
            SpellTarget target,
            SpellEffect effect,
            AuthoredCastContext context
    ) {
        if (target.isEntity()) {
            Entity entity = target.getEntity();
            if (entity == null || entity.isRemoved()
                    || (context != null && entity.level() != context.spellContext().getLevel())) {
                return false;
            }
            LivingEntity caster = source.getCaster();
            if (effect.getUseTag() == SpellPartTags.HARMFUL && entity instanceof LivingEntity living) {
                if (caster.isAlliedTo(living)) {
                    return false;
                }
                if (caster instanceof Player attacking && living instanceof Player defending
                        && !attacking.canHarmPlayer(defending)) {
                    return false;
                }
            }
            return true;
        }
        return target.isBlock() && effect.targetsBlocks();
    }

    private static CompoundTag payload(
            AuthoredCastContext context,
            SpellTarget target,
            ResourceLocation original,
            ResourceLocation complement,
            ModifiedSpellPart<SpellEffect> replacement
    ) {
        CompoundTag payload = new CompoundTag();
        payload.putString("original", original.toString());
        payload.putString("complement", complement.toString());
        payload.putString(
                "dimension",
                context.spellContext().getLevel().dimension().location().toString()
        );
        if (target.isEntity() && target.getEntity() != null) {
            payload.putUUID("target", target.getEntity().getUUID());
        } else if (target.isBlock()) {
            payload.putLong("block", target.getBlock().asLong());
        }
        CompoundTag attributes = new CompoundTag();
        for (Attribute attribute : replacement.getContainedAttributes()) {
            CompoundTag value = new CompoundTag();
            value.putFloat("value", replacement.getValue(attribute));
            value.putFloat("multiplier", replacement.getMultiplier(attribute));
            attributes.put(attribute.name(), value);
        }
        payload.put("attributes", attributes);
        return payload;
    }

    private static Optional<ServerLevel> loadedLevel(ServerPlayer player, CompoundTag payload) {
        ResourceLocation id = ResourceLocation.tryParse(payload.getString("dimension"));
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(player.server.getLevel(
                ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, id)
        ));
    }

    private static Optional<Entity> loadedTarget(ServerLevel level, CompoundTag payload) {
        if (!payload.hasUUID("target")) {
            return Optional.empty();
        }
        UUID targetId = payload.getUUID("target");
        return Optional.ofNullable(level.getEntity(targetId))
                .filter(entity -> !entity.isRemoved());
    }

    private static void restoreAttributes(
            CompoundTag attributes,
            ModifiedSpellPart<SpellEffect> replacement
    ) {
        for (Attribute attribute : replacement.getContainedAttributes()) {
            if (attributes.contains(attribute.name())) {
                CompoundTag value = attributes.getCompound(attribute.name());
                replacement.setValue(attribute, value.getFloat("value"));
                replacement.setMultiplier(attribute, value.getFloat("multiplier"));
            }
        }
    }

    private static InversionRelationship pair(
            ResourceLocation interpretation,
            String first,
            String second
    ) {
        return new InversionRelationship(interpretation, id(first), id(second));
    }

    private static ResourceLocation id(String value) {
        return ResourceLocation.tryParse(value);
    }
}
