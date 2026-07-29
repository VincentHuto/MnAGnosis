package com.vincenthuto.mnagnosis.common.item.armor;

import com.vincenthuto.mnagnosis.client.render.item.TesseractItemRenderer;
import com.vincenthuto.mnagnosis.common.item.FractalItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class TesseractItem extends FractalItem {
    public TesseractItem(Properties p_41383_) {
        super(p_41383_);
    }
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            final BlockEntityWithoutLevelRenderer myRenderer = new TesseractItemRenderer(null, null);

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return myRenderer;
            }
        });
    }
}
