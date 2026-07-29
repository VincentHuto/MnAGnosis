package com.vincenthuto.mnagnosis.common.progression.manuscript;

import net.minecraft.resources.ResourceLocation;

public enum AuthoredDiscipline {
    RELATION("relation"),
    DEFINITION("definition"),
    CONTINUANCE("continuance");

    private final ResourceLocation id;

    AuthoredDiscipline(String path) {
        this.id = ResourceLocation.fromNamespaceAndPath("mnagnosis", path);
    }

    public ResourceLocation id() {
        return id;
    }
}
