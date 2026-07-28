package com.vincenthuto.mnagnosis.common.item;

public final class IneffableRobesSlot {

    public static final String BODY = "body";

    private IneffableRobesSlot() {
    }

    public static boolean isBody(String identifier) {
        return BODY.equals(identifier);
    }
}
