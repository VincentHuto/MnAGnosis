package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableHudFrameCompositionTest {

    @Test
    void portalAndResourcesRemainBetweenTheCorrectFrameLayers()
            throws IOException {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/mnagnosis/client/authorship/"
                        + "IneffableHudRenderer.java"
        ));
        int backing = renderer.indexOf(
                "IneffableHudConcept.backingTexture()"
        );
        int portal = renderer.indexOf("IneffableHudPortalRenderer.render");
        int frame = renderer.indexOf("IneffableHudConcept.frameTexture()");
        int disruption = renderer.indexOf(
                "IneffableHudConcept.disruptionTexture(state)"
        );
        int mana = renderer.indexOf("IneffableHudConcept.manaTexture()");
        int leftCap = renderer.indexOf("leftManaCapX(manaWidth)");
        int rightCap = renderer.indexOf("manaCapX(manaWidth)");

        assertTrue(backing >= 0);
        assertTrue(portal > backing);
        assertTrue(frame > portal);
        assertTrue(disruption > frame);
        assertTrue(mana > disruption);
        assertTrue(leftCap > mana);
        assertTrue(rightCap > leftCap);
    }
}
