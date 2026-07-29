package com.vincenthuto.mnagnosis.common.authorship.state;

import com.vincenthuto.mnagnosis.common.authorship.AuthorshipRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class IneffableCastingState implements IIneffableCastingState {

    private static final int SCHEMA_VERSION = 1;
    private static final String SCHEMA_VERSION_KEY = "schema_version";
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
        root.putInt(SCHEMA_VERSION_KEY, SCHEMA_VERSION);
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
        ledger = new ContradictionLedger();
        selectedInterpretations.clear();
        declaredClosure = null;
        if (root.getInt(SCHEMA_VERSION_KEY) != SCHEMA_VERSION) {
            return;
        }

        ledger = ContradictionLedger.load(root.getCompound(LEDGER_KEY));
        ledger.retain(debt -> AuthorshipRegistry.isKnownAuthorship(
                debt.lawId(), debt.interpretationId())
                || ExternalContradictionType.isTypedExternal(debt));

        CompoundTag selections = root.getCompound(SELECTIONS_KEY);
        for (String fingerprint : selections.getAllKeys()) {
            ResourceLocation interpretation =
                    ResourceLocation.tryParse(selections.getString(fingerprint));
            if (!fingerprint.isBlank() && interpretation != null
                    && AuthorshipRegistry.isKnownInterpretation(interpretation)) {
                selectedInterpretations.put(fingerprint, interpretation);
            }
        }

        if (root.hasUUID(DECLARED_CLOSURE_KEY)) {
            UUID requested = root.getUUID(DECLARED_CLOSURE_KEY);
            declaredClosure = ledger.entries().stream()
                    .anyMatch(debt -> debt.id().equals(requested))
                    ? requested : null;
        }
    }

    @Override
    public void copyFrom(IIneffableCastingState source) {
        deserializeNBT(source.serializeNBT());
    }
}
