package com.vincenthuto.mnagnosis.client.render.armor;

public record IneffableRobePresentation(
        boolean armoredBody,
        boolean hoodVisible
) {

    public static IneffableRobePresentation from(
            boolean head,
            boolean chest,
            boolean legs,
            boolean feet
    ) {
        return new IneffableRobePresentation(chest || legs, !head);
    }
}
