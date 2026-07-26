package com.vincenthuto.mnagnosis.common.authorship.state;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class IneffableCastingState implements IIneffableCastingState {

    private static final String LEDGER_KEY = "ledger";
    private static final String SELECTIONS_KEY = "selections";
    private static final String DECLARED_CLOSURE_KEY = "declared_closure";

    private ContradictionLedger ledger = new ContradictionLedger();
    private final Map<String, ResourceLocation> selectedInterpretations = new HashMap<>();
    private UUID declaredClosure;

    @Override
    public ContradictionLedger ledger() {
        return ledger;
    }

    @Override
    public Optional<ResourceLocation> selectedInterpretation(String fingerprint) {
        return Optional.ofNullable(selectedInterpretations.get(fingerprint));
    }

    @Override
    public void selectInterpretation(String fingerprint, ResourceLocation interpretationId) {
        if (fingerprint == null || fingerprint.isBlank()) {
            throw new IllegalArgumentException("Spell fingerprint cannot be blank");
        }
        selectedInterpretations.put(fingerprint, Objects.requireNonNull(interpretationId));
    }

    @Override
    public Optional<UUID> declaredClosure() {
        return Optional.ofNullable(declaredClosure);
    }

    @Override
    public void declareClosure(UUID contradictionId) {
        declaredClosure = Objects.requireNonNull(contradictionId);
    }

    @Override
    public void clearDeclaredClosure() {
        declaredClosure = null;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag root = new CompoundTag();
        root.put(LEDGER_KEY, ledger.save());

        CompoundTag selections = new CompoundTag();
        selectedInterpretations.forEach(
                (fingerprint, interpretation) ->
                        selections.putString(fingerprint, interpretation.toString())
        );
        root.put(SELECTIONS_KEY, selections);

        if (declaredClosure != null) {
            root.putUUID(DECLARED_CLOSURE_KEY, declaredClosure);
        }
        return root;
    }

    @Override
    public void deserializeNBT(CompoundTag root) {
        ledger = ContradictionLedger.load(root.getCompound(LEDGER_KEY));
        selectedInterpretations.clear();

        CompoundTag selections = root.getCompound(SELECTIONS_KEY);
        for (String fingerprint : selections.getAllKeys()) {
            ResourceLocation interpretation =
                    ResourceLocation.tryParse(selections.getString(fingerprint));
            if (!fingerprint.isBlank() && interpretation != null) {
                selectedInterpretations.put(fingerprint, interpretation);
            }
        }

        declaredClosure = root.hasUUID(DECLARED_CLOSURE_KEY)
                ? root.getUUID(DECLARED_CLOSURE_KEY)
                : null;
    }

    @Override
    public void copyFrom(IIneffableCastingState source) {
        deserializeNBT(source.serializeNBT());
    }
}
