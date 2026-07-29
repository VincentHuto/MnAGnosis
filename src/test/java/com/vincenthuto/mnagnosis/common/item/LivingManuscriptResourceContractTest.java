package com.vincenthuto.mnagnosis.common.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class LivingManuscriptResourceContractTest {
    private static final Path RESOURCES = Path.of("src/main/resources");

    @Test
    void livingManuscriptHasModelRecipeAndPlayerFacingCopy() throws Exception {
        JsonObject model = json(
                "assets/mnagnosis/models/item/living_manuscript.json");
        JsonObject recipe = json(
                "data/mnagnosis/recipes/living_manuscript.json");
        String language = Files.readString(RESOURCES.resolve(
                "assets/mnagnosis/lang/en_us.json"));

        assertEquals("minecraft:item/generated", model.get("parent").getAsString());
        assertEquals(
                "minecraft:item/book",
                model.getAsJsonObject("textures").get("layer0").getAsString());
        assertEquals(
                "minecraft:crafting_shapeless",
                recipe.get("type").getAsString());
        assertTrue(recipe.toString().contains("minecraft:book"));
        assertTrue(recipe.toString().contains("mnagnosis:primal_mote"));
        assertTrue(language.contains("\"item.mnagnosis.living_manuscript\""));
        assertTrue(language.contains("\"screen.mnagnosis.manuscript.definition\""));
        assertTrue(language.contains("\"proof.mnagnosis.revelation.definition\""));
    }

    private static JsonObject json(String relative) throws Exception {
        return JsonParser.parseString(Files.readString(RESOURCES.resolve(relative)))
                .getAsJsonObject();
    }
}
