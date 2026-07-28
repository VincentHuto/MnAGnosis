package com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothSunEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class YaldabaothSunModel extends GeoModel<YaldabaothSunEntity> {

    private static final ResourceLocation MODEL =
            MnAGnosis.rloc("geo/entity/yaldabaoth_sun.geo.json");
    private static final ResourceLocation TEXTURE =
            MnAGnosis.rloc("textures/entity/yaldabaoth/yaldabaoth_sun.png");
    private static final ResourceLocation ANIMATIONS =
            MnAGnosis.rloc("animations/entity/yaldabaoth_sun.animation.json");

    @Override
    public ResourceLocation getModelResource(YaldabaothSunEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(YaldabaothSunEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(YaldabaothSunEntity entity) {
        return ANIMATIONS;
    }
}
