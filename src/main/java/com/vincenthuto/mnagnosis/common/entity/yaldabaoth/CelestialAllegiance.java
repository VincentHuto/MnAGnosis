package com.vincenthuto.mnagnosis.common.entity.yaldabaoth;

import java.util.Locale;

public enum CelestialAllegiance {
    HOSTILE,
    DORMANT,
    WITNESS;

    public String serializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public static CelestialAllegiance fromSerializedName(String name) {
        if (name == null) {
            return HOSTILE;
        }
        for (CelestialAllegiance value : values()) {
            if (value.serializedName().equals(name)) {
                return value;
            }
        }
        return HOSTILE;
    }
}
