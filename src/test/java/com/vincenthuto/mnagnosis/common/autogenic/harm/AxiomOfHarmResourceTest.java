package com.vincenthuto.mnagnosis.common.autogenic.harm;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AxiomOfHarmResourceTest {
    private static final Path RESOURCES = Path.of("src/main/resources");

    @Test
    void recipeIsTheExactTierSixModifierContract() throws Exception {
        JsonObject recipe = JsonParser.parseString(Files.readString(
                RESOURCES.resolve("data/mnagnosis/recipes/axiom_of_harm.json")
        )).getAsJsonObject();

        assertEquals("mna:modifier", recipe.get("type").getAsString());
        assertEquals(6, recipe.get("tier").getAsInt());
        assertEquals("mnagnosis:axiom_of_harm", recipe.get("output").getAsString());
        assertEquals(List.of(
                "mnagnosis:tesseract",
                "minecraft:flint_and_steel",
                "minecraft:fermented_spider_eye",
                "minecraft:nether_star",
                "minecraft:black_concrete",
                "minecraft:white_concrete"
        ), strings(recipe.getAsJsonArray("items")));
        assertEquals(List.of(
                "mna:manaweave_patterns/triangle",
                "mna:manaweave_patterns/inverted_triangle"
        ), strings(recipe.getAsJsonArray("patterns")));
    }

    @Test
    void iconAndPlayerFacingCopyArePackaged() throws Exception {
        var image = ImageIO.read(RESOURCES.resolve(
                "assets/mnagnosis/textures/spell/modifier/axiom_of_harm.png"
        ).toFile());
        assertNotNull(image);
        assertTrue(image.getWidth() > 0 && image.getHeight() > 0);
        assertTrue(image.getColorModel().hasAlpha());

        JsonObject language = JsonParser.parseString(Files.readString(
                RESOURCES.resolve("assets/mnagnosis/lang/en_us.json")
        )).getAsJsonObject();
        assertEquals(
                "Axiom of Harm",
                language.get("modifier.mnagnosis.axiom_of_harm").getAsString()
        );
        assertEquals(
                "The first compatible harm denies one native immunity.",
                language.get("modifier.mnagnosis.axiom_of_harm.description").getAsString()
        );
    }

    private static List<String> strings(JsonArray values) {
        return values.asList().stream()
                .map(value -> value.getAsString())
                .toList();
    }
}
