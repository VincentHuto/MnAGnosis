package com.vincenthuto.mnagnosis.common.architectonics.instrument;

import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;

import java.util.UUID;

public record LatticeSnapshot(
        int schemaVersion,
        ReassembledPattern pattern,
        UUID itemNonce
) {
}
