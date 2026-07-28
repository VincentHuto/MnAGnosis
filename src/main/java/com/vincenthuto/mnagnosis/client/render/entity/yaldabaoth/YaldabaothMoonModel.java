package com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothMoonEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class YaldabaothMoonModel extends GeoModel<YaldabaothMoonEntity> {

    private static final ResourceLocation MODEL =
            MnAGnosis.rloc("geo/entity/yaldabaoth_moon.geo.json");
    private static final ResourceLocation TEXTURE =
            MnAGnosis.rloc("textures/entity/yaldabaoth/yaldabaoth_moon.png");
    private static final ResourceLocation ANIMATIONS =
            MnAGnosis.rloc("animations/entity/yaldabaoth_moon.animation.json");

    @Override
    public ResourceLocation getModelResource(YaldabaothMoonEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(YaldabaothMoonEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(YaldabaothMoonEntity entity) {
        return ANIMATIONS;
    }
}
