package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableHudPortalRendererTest {

    @Test
    void portalQuadMatchesTheCompleteManaChannel() {
        assertEquals(80, IneffableHudPortalRenderer.X);
        assertEquals(52, IneffableHudPortalRenderer.Y);
        assertEquals(790, IneffableHudPortalRenderer.WIDTH);
        assertEquals(54, IneffableHudPortalRenderer.HEIGHT);
    }

    @Test
    void animationTimeIncludesPartialTickAndUsesSeconds() {
        assertEquals(
                4.03125F,
                IneffableHudPortalRenderer.animationSeconds(80L, 0.625F),
                0.0001F
        );
    }

    @Test
    void portalCompositesBetweenBaseAndLiveLayers() throws IOException {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/mnagnosis/client/authorship/"
                        + "IneffableHudRenderer.java"
        ));
        int base = renderer.indexOf(
                "blitFull(graphics, IneffableHudConcept.baseTexture())"
        );
        int portal = renderer.indexOf("IneffableHudPortalRenderer.render");
        int disruption = renderer.indexOf(
                "IneffableHudConcept.disruptionTexture(state)"
        );
        int mana = renderer.indexOf("IneffableHudConcept.manaTexture()");

        assertTrue(base >= 0);
        assertTrue(portal > base);
        assertTrue(disruption > portal);
        assertTrue(mana > portal);
    }
}
