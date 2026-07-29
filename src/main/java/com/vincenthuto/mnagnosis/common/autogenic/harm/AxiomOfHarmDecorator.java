package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.mna.api.spells.ComponentApplicationResult;
import com.vincenthuto.mnagnosis.common.autogenic.AutogenicCastRuntime;
import com.vincenthuto.mnagnosis.common.autogenic.AutogenicProgression;
import net.minecraft.server.level.ServerPlayer;

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
        ComponentApplicationResult result;
        HarmInvocationScope.Outcome outcome;
        try {
            result = nativeApplication.get();
            outcome = scope.outcome();
        } finally {
            scope.close();
        }
        if (completedCrossing(result, outcome)
                && invocation.source().getCaster() instanceof ServerPlayer player) {
            AutogenicProgression.grantAxiomProof(
                    player,
                    invocation.livingTarget().getUUID()
            );
        }
        return result;
    }

    static boolean completedCrossing(
            ComponentApplicationResult result,
            HarmInvocationScope.Outcome outcome
    ) {
        return result == ComponentApplicationResult.SUCCESS
                && outcome.gateConsumed()
                && outcome.nativeSucceeded();
    }
}
