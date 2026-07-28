package com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth;

import com.vincenthuto.mnagnosis.MnAGnosis;
import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class YaldabaothModel extends GeoModel<YaldabaothEntity> {

    private static final ResourceLocation MODEL =
            MnAGnosis.rloc("geo/entity/yaldabaoth.geo.json");
    private static final ResourceLocation TEXTURE =
            MnAGnosis.rloc("textures/entity/yaldabaoth/yaldabaoth.png");
    private static final ResourceLocation ANIMATIONS =
            MnAGnosis.rloc("animations/entity/yaldabaoth.animation.json");

    @Override
    public ResourceLocation getModelResource(YaldabaothEntity entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(YaldabaothEntity entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(YaldabaothEntity entity) {
        return ANIMATIONS;
    }
}
