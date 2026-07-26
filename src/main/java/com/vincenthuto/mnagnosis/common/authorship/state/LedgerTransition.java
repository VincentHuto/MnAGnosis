package com.vincenthuto.mnagnosis.common.authorship.state;

import java.util.List;

public record LedgerTransition(
        List<Contradiction> vented,
        List<Contradiction> remaining
) {

    public LedgerTransition {
        vented = List.copyOf(vented);
        remaining = List.copyOf(remaining);
    }
}
