package com.vincenthuto.mnagnosis.common.authorship.state;

import java.util.Optional;
import java.util.UUID;

public final class ExternalContradictionService {
    public <T> ExternalDebtTransition record(
            ContradictionLedger ledger,
            ExternalContradictionType<T> type,
            UUID actionId,
            T payload,
            long order) {
        if (ledger.entries().stream().anyMatch(debt -> debt.id().equals(actionId))) {
            return new ExternalDebtTransition(
                    ExternalDebtResult.ALREADY_RECORDED, Optional.empty(), Optional.empty());
        }
        Contradiction created = new Contradiction(
                actionId,
                type.handlerId(),
                type.variantId(),
                type.paradox(),
                ContradictionLedger.MAX_SAFE_CASTS,
                order,
                type.encode(payload));
        LedgerTransition transition = ledger.add(created);
        return new ExternalDebtTransition(
                ExternalDebtResult.APPLIED,
                Optional.of(created),
                transition.vented().stream().findFirst());
    }
}
