package com.vincenthuto.mnagnosis.common.progression.manuscript;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public final class DisciplineProgressionRegistry {
    private final Map<AuthoredDiscipline, DisciplineProgressionDefinition> definitions =
            new EnumMap<>(AuthoredDiscipline.class);

    public void register(DisciplineProgressionDefinition definition) {
        if (definitions.putIfAbsent(definition.discipline(), definition) != null) {
            throw new IllegalStateException(
                    "A progression definition is already registered for " + definition.discipline());
        }
    }

    public Optional<DisciplineProgressionDefinition> definition(AuthoredDiscipline discipline) {
        return Optional.ofNullable(definitions.get(discipline));
    }

    public Map<AuthoredDiscipline, DisciplineProgressionDefinition> definitions() {
        return Collections.unmodifiableMap(definitions);
    }
}
