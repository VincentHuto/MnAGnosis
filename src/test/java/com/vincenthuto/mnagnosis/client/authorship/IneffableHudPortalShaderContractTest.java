package com.vincenthuto.mnagnosis.client.authorship;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableHudPortalShaderContractTest {

    private static final Path ROOT = Path.of("src/main");
    private static final Path SHADERS = ROOT.resolve(
            "resources/assets/mnagnosis/shaders/core"
    );

    @Test
    void portalProgramMatchesItsTexturelessHudVertexContract()
            throws IOException {
        JsonObject program = json("ineffable_hud_portal.json");

        assertEquals(
                "mnagnosis:ineffable_hud_portal",
                program.get("vertex").getAsString()
        );
        assertEquals(
                "mnagnosis:ineffable_hud_portal",
                program.get("fragment").getAsString()
        );
        assertEquals(
                List.of("Position", "UV0"),
                StreamSupport.stream(
                                program.getAsJsonArray("attributes")
                                        .spliterator(),
                                false
                        )
                        .map(element -> element.getAsString())
                        .toList()
        );
        Set<String> uniforms = StreamSupport.stream(
                        program.getAsJsonArray("uniforms").spliterator(),
                        false
                )
                .map(element -> element.getAsJsonObject()
                        .get("name").getAsString())
                .collect(Collectors.toSet());
        assertEquals(
                Set.of(
                        "ModelViewMat",
                        "ProjMat",
                        "PortalTime",
                        "PortalOpacity"
                ),
                uniforms
        );
    }

    @Test
    void portalShaderBuildsSparseIneffableParallax() throws IOException {
        String vertex = read("ineffable_hud_portal.vsh");
        String fragment = read("ineffable_hud_portal.fsh");

        assertTrue(vertex.contains("in vec2 UV0"));
        assertTrue(vertex.contains("texCoord = UV0"));
        assertTrue(fragment.contains("PortalTime"));
        assertTrue(fragment.contains("PortalOpacity"));
        assertTrue(fragment.contains("hash21"));
        assertTrue(fragment.contains("starLayer"));
        assertTrue(fragment.contains("warpedBand"));
        assertTrue(fragment.contains("790.0 / 54.0"));
        assertTrue(fragment.contains("vec3(0.0, 0.72, 0.83)"));
    }

    @Test
    void coreShaderRegistryExposesThePortalProgram() throws IOException {
        String coreShaders = Files.readString(ROOT.resolve(
                "java/com/vincenthuto/mnagnosis/client/shader/core/"
                        + "CoreShaders.java"
        ));

        assertTrue(coreShaders.contains("ineffableHudPortal"));
        assertTrue(coreShaders.contains("rloc(\"ineffable_hud_portal\")"));
        assertTrue(coreShaders.contains("DefaultVertexFormat.POSITION_TEX"));
    }

    private static JsonObject json(String name) throws IOException {
        return JsonParser.parseString(read(name)).getAsJsonObject();
    }

    private static String read(String name) throws IOException {
        return Files.readString(SHADERS.resolve(name));
    }
}
