package com.vincenthuto.mnagnosis.common.faction;

import com.mna.api.capabilities.resource.ICastingResource;
import com.mna.api.capabilities.resource.SimpleCastingResource;
import com.mna.api.config.GeneralConfigValues;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public class IneffableMana extends SimpleCastingResource {

    private static final String AMOUNT_KEY = "amount";
    private static final String MAXIMUM_KEY = "maximum";
    private static final String MAXIMUM_MODIFIERS_KEY = "maximum_modifiers";
    private static final String REGENERATION_MODIFIERS_KEY = "regeneration_modifiers";
    private static final String PARADOX_KEY = "mnagnosis_paradox";
    private static final String KEY_KEY = "key";
    private static final String VALUE_KEY = "value";

    private float paradox;

    public IneffableMana() {
        super(GeneralConfigValues.TotalManaRegenTicks);
    }

    @Override
    public int getRegenerationRate(LivingEntity caster) {
        return (int) (GeneralConfigValues.TotalManaRegenTicks * this.getRegenerationModifier(caster));
    }

    @Override
    public ResourceLocation getRegistryName() {
        return IneffableFactionRegistry.CASTING_RESOURCE_ID;
    }

    public float getParadox() {
        return paradox;
    }

    public void setParadox(float paradox) {
        float clamped = clamp(paradox, 0.0F, getMaxAmount());
        if (this.paradox != clamped) {
            this.paradox = clamped;
            setNeedsSync();
        }
        setAmount(getAmount());
    }

    public float getSafeMaximum() {
        return Math.max(0.0F, getMaxAmount() - paradox);
    }

    @Override
    public void setAmount(float amount) {
        super.setAmount(clamp(amount, 0.0F, getSafeMaximum()));
    }

    @Override
    public void restore(float amount) {
        setAmount(getAmount() + amount);
    }

    @Override
    public void setMaxAmount(float amount) {
        super.setMaxAmount(amount);
        setParadox(paradox);
    }

    @Override
    public void setMaxAmountByLevel(int level) {
        this.setMaxAmount(100 + 20 * level);
    }

    @Override
    public void addModifier(String key, float amount) {
        super.addModifier(key, amount);
        setParadox(paradox);
    }

    @Override
    public void removeModifier(String key) {
        super.removeModifier(key);
        setParadox(paradox);
    }

    @Override
    public void clearModifiers() {
        super.clearModifiers();
        setParadox(paradox);
    }

    @Override
    public void copyFrom(ICastingResource source) {
        if (!getRegistryName().equals(source.getRegistryName())) {
            return;
        }

        super.clearModifiers();
        super.setMaxAmount(source.getMaxAmountBaseline());
        source.getModifiers().forEach(super::addModifier);
        super.clearRegenerationModifiers();
        source.getRegenerationModifiers().forEach(super::addRegenerationModifier);
        setParadox(source instanceof IneffableMana ineffable ? ineffable.getParadox() : 0.0F);
        setAmount(source.getAmount());
    }

    @Override
    public void writeNBT(CompoundTag root) {
        CompoundTag resource = new CompoundTag();
        resource.putFloat(AMOUNT_KEY, getAmount());
        resource.putFloat(MAXIMUM_KEY, getMaxAmountBaseline());
        resource.put(MAXIMUM_MODIFIERS_KEY, writeModifiers(getModifiers()));
        resource.put(REGENERATION_MODIFIERS_KEY, writeModifiers(getRegenerationModifiers()));
        resource.putFloat(PARADOX_KEY, paradox);
        root.put(getRegistryName().toString(), resource);
    }

    @Override
    public void readNBT(CompoundTag root) {
        String resourceKey = getRegistryName().toString();
        if (!root.contains(resourceKey, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag resource = root.getCompound(resourceKey);
        super.clearModifiers();
        super.clearRegenerationModifiers();
        super.setMaxAmount(resource.getFloat(MAXIMUM_KEY));
        readModifiers(resource.getList(MAXIMUM_MODIFIERS_KEY, Tag.TAG_COMPOUND))
                .forEach(super::addModifier);
        readModifiers(resource.getList(REGENERATION_MODIFIERS_KEY, Tag.TAG_COMPOUND))
                .forEach(super::addRegenerationModifier);
        setParadox(resource.getFloat(PARADOX_KEY));
        setAmount(resource.getFloat(AMOUNT_KEY));
    }

    private static ListTag writeModifiers(Map<String, Float> modifiers) {
        ListTag serialized = new ListTag();
        modifiers.forEach((key, value) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString(KEY_KEY, key);
            entry.putFloat(VALUE_KEY, value);
            serialized.add(entry);
        });
        return serialized;
    }

    private static Map<String, Float> readModifiers(ListTag serialized) {
        java.util.HashMap<String, Float> modifiers = new java.util.HashMap<>();
        for (Tag element : serialized) {
            CompoundTag entry = (CompoundTag) element;
            modifiers.put(entry.getString(KEY_KEY), entry.getFloat(VALUE_KEY));
        }
        return modifiers;
    }

    private static float clamp(float value, float minimum, float maximum) {
        if (!Float.isFinite(value)) {
            return minimum;
        }
        return Math.max(minimum, Math.min(value, maximum));
    }
}
