package com.vincenthuto.mnagnosis.common.autogenic.harm;

public record HarmTargetFacts(
        boolean present,
        boolean alive,
        boolean removed,
        boolean loaded,
        boolean sameDimension,
        boolean invulnerable,
        boolean creativeOrSpectator,
        boolean allied,
        boolean pvpAllowed
) {
}
