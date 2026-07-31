package com.vincenthuto.mnagnosis.common.architectonics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchitectonicResourceContractTest {
    private static final Path RESOURCES = Path.of("src/main/resources");

    @Test
    void latticeHasAHandheldModelAndCraftingContract() throws Exception {
        JsonObject model = json(
                "assets/mnagnosis/models/item/unbounded_lattice.json");
        JsonObject recipe = json(
                "data/mnagnosis/recipes/unbounded_lattice.json");

        assertEquals(
                "minecraft:item/handheld",
                model.get("parent").getAsString());
        assertEquals(
                "mnagnosis:item/unbounded_lattice",
                model.getAsJsonObject("textures")
                        .get("layer0").getAsString());
        assertEquals(
                "minecraft:crafting_shaped",
                recipe.get("type").getAsString());
        assertEquals(
                "mnagnosis:unbounded_lattice",
                recipe.getAsJsonObject("result")
                        .get("item").getAsString());
    }

    @Test
    void reassembledLandIsACompleteTierSixComponentResource()
            throws Exception {
        JsonObject recipe = json(
                "data/mnagnosis/recipes/components/reassembled_land.json");
        assertEquals("mna:component", recipe.get("type").getAsString());
        assertEquals(6, recipe.get("tier").getAsInt());
        assertEquals(
                "mnagnosis:components/reassembled_land",
                recipe.get("output").getAsString());

        var lattice = ImageIO.read(RESOURCES.resolve(
                "assets/mnagnosis/textures/item/unbounded_lattice.png")
                .toFile());
        var component = ImageIO.read(RESOURCES.resolve(
                "assets/mnagnosis/textures/spell/component/"
                        + "reassembled_land.png").toFile());
        assertNotNull(lattice);
        assertNotNull(component);
        assertTrue(lattice.getColorModel().hasAlpha());
        assertTrue(component.getColorModel().hasAlpha());

        JsonObject language = json("assets/mnagnosis/lang/en_us.json");
        assertEquals(
                "Unbounded Lattice",
                language.get("item.mnagnosis.unbounded_lattice")
                        .getAsString());
        assertEquals(
                "Reassembled Land",
                language.get("mnagnosis:components/reassembled_land")
                        .getAsString());
    }

    private static JsonObject json(String relative) throws Exception {
        return JsonParser.parseString(Files.readString(
                RESOURCES.resolve(relative))).getAsJsonObject();
    }
}
