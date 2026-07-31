package com.vincenthuto.mnagnosis.common.architectonics;

import com.vincenthuto.mnagnosis.common.authorship.AuthorshipCastingService;
import com.vincenthuto.mnagnosis.common.item.UnboundedLatticeItem;
import com.vincenthuto.mnagnosis.common.registry.ItemRegistry;

/**
 * Registers Architectonics integrations after Forge registries are available.
 */
public final class ArchitectonicBootstrap {
    private static boolean bootstrapped;

    private ArchitectonicBootstrap() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        AuthorshipCastingService.instrumentRegistry().register(
                (UnboundedLatticeItem)
                        ItemRegistry.UNBOUNDED_LATTICE.get());
        bootstrapped = true;
    }
}
