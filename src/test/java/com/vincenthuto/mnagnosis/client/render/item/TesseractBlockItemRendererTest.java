package com.vincenthuto.mnagnosis.client.render.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.mnagnosis.common.item.FractalEntityItem;
import com.vincenthuto.mnagnosis.common.item.TesseractBlockItem;
import net.minecraft.world.item.BlockItem;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TesseractBlockItemRendererTest {

    private static final Path MAIN = Path.of("src/main");

    @Test
    void blockItemExposesTheAnimatedTesseractRenderer() throws Exception {
        String item = Files.readString(
                MAIN.resolve(
                        "java/com/vincenthuto/mnagnosis/common/item/"
                                + "TesseractBlockItem.java"
                )
        );

        assertTrue(
                BlockItem.class.isAssignableFrom(
                        TesseractBlockItem.class
                )
        );
        assertTrue(
                FractalEntityItem.class.isAssignableFrom(
                        TesseractBlockItem.class
                )
        );
        assertTrue(item.contains("initializeClient("));
        assertTrue(item.contains("new TesseractItemRenderer(null, null)"));
        assertTrue(item.contains("getCustomRenderer()"));
    }

    @Test
    void builtinEntityModelAndRegistryRouteToTheCustomBlockItem()
            throws Exception {
        Path assets = MAIN.resolve("resources/assets/mnagnosis");
        JsonObject model = JsonParser.parseString(
                Files.readString(
                        assets.resolve("models/item/tesseract_block.json")
                )
        ).getAsJsonObject();
        String registry = Files.readString(
                MAIN.resolve(
                        "java/com/vincenthuto/mnagnosis/common/registry/"
                                + "ItemRegistry.java"
                )
        );

        assertEquals("builtin/entity", model.get("parent").getAsString());
        assertTrue(registry.contains("new TesseractBlockItem("));
    }
}
