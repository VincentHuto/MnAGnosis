package com.vincenthuto.mnagnosis.common.spell.gravity.shift;

import javax.annotation.Nullable;

/**
 * Bridge from the movement mixin to server-side gravity traversal.
 */
public interface GravityCollisionAccess {

    @Nullable
    GravityMoveResult mnagnosis$gravityMoveResult();
}
