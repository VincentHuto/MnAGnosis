package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import java.util.UUID;

public record AssemblyResult(
        UUID receiptId,
        Failure failure
) {
    public boolean placed() {
        return receiptId != null && failure == Failure.NONE;
    }

    public boolean satisfied() {
        return receiptId == null
                && failure == Failure.ALREADY_SATISFIED;
    }

    public boolean successful() {
        return placed() || satisfied();
    }

    public static AssemblyResult placed(UUID receiptId) {
        return new AssemblyResult(receiptId, Failure.NONE);
    }

    public static AssemblyResult rejected(Failure failure) {
        return new AssemblyResult(null, failure);
    }

    public static AssemblyResult alreadySatisfied() {
        return new AssemblyResult(
                null, Failure.ALREADY_SATISFIED);
    }

    public enum Failure {
        NONE,
        ALREADY_SATISFIED,
        INVALID_PLAN,
        RECEIPT_CAP,
        UNLOADED,
        DENIED,
        BLOCKED_TARGET,
        INSUFFICIENT_MATTER,
        ROLLED_BACK
    }

    public enum Returned {
        MANUAL,
        AUTOMATIC,
        NOT_FOUND,
        DENIED,
        UNLOADED,
        CONFLICTED,
        ROLLED_BACK
    }
}
