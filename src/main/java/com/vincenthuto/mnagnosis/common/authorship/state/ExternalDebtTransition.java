package com.vincenthuto.mnagnosis.common.authorship.state;

import java.util.Optional;

public record ExternalDebtTransition(
        ExternalDebtResult result,
        Optional<Contradiction> created,
        Optional<Contradiction> vented) {

    public ExternalDebtTransition {
        created = created == null ? Optional.empty() : created;
        vented = vented == null ? Optional.empty() : vented;
    }
}
