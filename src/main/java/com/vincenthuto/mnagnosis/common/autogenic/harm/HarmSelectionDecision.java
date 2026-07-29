package com.vincenthuto.mnagnosis.common.autogenic.harm;

import java.util.Optional;

public record HarmSelectionDecision(
        Optional<HarmSelection> selection,
        Failure failure
) {
    public HarmSelectionDecision {
        selection = selection == null ? Optional.empty() : selection;
        if (failure == null
                || (selection.isPresent() && failure != Failure.NONE)
                || (selection.isEmpty() && failure == Failure.NONE)) {
            throw new IllegalArgumentException("Inconsistent harm selection");
        }
    }

    public static HarmSelectionDecision selected(HarmSelection selection) {
        return new HarmSelectionDecision(Optional.of(selection), Failure.NONE);
    }

    public static HarmSelectionDecision failed(Failure failure) {
        return new HarmSelectionDecision(Optional.empty(), failure);
    }

    public enum Failure {
        NONE,
        ORDER_UNAVAILABLE,
        NO_COMPATIBLE_HARM
    }
}
