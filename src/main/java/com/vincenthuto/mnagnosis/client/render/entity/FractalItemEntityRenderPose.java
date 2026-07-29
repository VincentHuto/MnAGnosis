package com.vincenthuto.mnagnosis.client.render.entity;

import com.vincenthuto.mnagnosis.common.entity.item.FractalItemEntityTraits;

public record FractalItemEntityRenderPose(
        float verticalTranslation,
        float yawRadians,
        float scale,
        boolean fullBright
) {

    public static FractalItemEntityRenderPose from(
            FractalItemEntityTraits traits,
            float ageInTicks,
            float groundScaleY
    ) {
        return new FractalItemEntityRenderPose(
                0.25F * groundScaleY
                        + traits.verticalOffset()
                        + traits.bobOffset(ageInTicks),
                traits.rotation(ageInTicks),
                traits.renderScale(),
                traits.fullBright()
        );
    }
}
