package com.vincenthuto.mnagnosis.client.render.entity.yaldabaoth;

import com.vincenthuto.mnagnosis.common.entity.yaldabaoth.YaldabaothEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class YaldabaothRenderer extends GeoEntityRenderer<YaldabaothEntity> {

    public YaldabaothRenderer(EntityRendererProvider.Context context) {
        super(context, new YaldabaothModel());
        this.shadowRadius = 2.0F;
    }
}
