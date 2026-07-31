package com.vincenthuto.mnagnosis.client.manuscript;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class LivingManuscriptScreenContractTest {
    @Test
    void screenProvidesTabsKeyboardStagesProofsAndVeiledFutureCopy() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/mnagnosis/client/manuscript/"
                        + "LivingManuscriptScreen.java"));

        assertTrue(source.contains("mouseClicked"));
        assertTrue(source.contains("GLFW_KEY_LEFT"));
        assertTrue(source.contains("GLFW_KEY_RIGHT"));
        assertTrue(source.contains("ManuscriptStage.values()"));
        assertTrue(source.contains("proofKey("));
        assertTrue(source.contains("model.guidanceKey()"));
    }
}
