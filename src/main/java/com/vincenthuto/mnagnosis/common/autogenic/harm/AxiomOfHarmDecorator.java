package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.ComponentApplicationResult;
import com.vincenthuto.mnagnosis.common.autogenic.AutogenicCastRuntime;

import java.util.function.Supplier;

public final class AxiomOfHarmDecorator {
    private AxiomOfHarmDecorator() {
    }

    public static ComponentApplicationResult apply(
            AutogenicCastRuntime.ComponentInvocation invocation,
            Supplier<ComponentApplicationResult> nativeApplication
    ) {
        if (invocation == null
                || HarmTargetPolicy.evaluate(
                invocation.source(),
                invocation.livingTarget()
        ) != HarmTargetDecision.ALLOW) {
            return nativeApplication.get();
        }
        HarmSelection selection = invocation.selection();
        HarmInvocationScope scope = HarmInvocationScope.open(
                invocation.permit(),
                selection.componentIndex(),
                selection.componentId(),
                invocation.part(),
                invocation.context(),
                invocation.livingTarget().getUUID(),
                selection.adapterId(),
                selection.gate()
        );
        try {
            return nativeApplication.get();
        } finally {
            scope.close();
        }
    }
}
