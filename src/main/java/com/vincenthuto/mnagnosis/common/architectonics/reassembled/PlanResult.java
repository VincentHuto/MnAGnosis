package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

public sealed interface PlanResult {
    record Success(ReassembledPlan plan) implements PlanResult {
    }

    record Rejected(Failure failure) implements PlanResult {
    }

    enum Failure {
        INVALID_PARAMETERS,
        TOO_MANY_CELLS
    }
}
