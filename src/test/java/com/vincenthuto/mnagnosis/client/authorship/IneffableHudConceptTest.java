package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableHudConceptTest {

    private static final Path TEXTURES = Path.of(
            "src/main/resources/assets/mnagnosis/textures/mna"
    );

    @Test
    void conceptLayersKeepTheReferenceResolution() throws IOException {
        assertEquals(976, IneffableHudConcept.SOURCE_WIDTH);
        assertEquals(158, IneffableHudConcept.SOURCE_HEIGHT);
        assertEquals(153, IneffableHudConcept.DISPLAY_WIDTH);
        assertEquals(25, IneffableHudConcept.DISPLAY_HEIGHT);
        assertEquals(20, IneffableHudConcept.BADGE_DISPLAY_SIZE);

        for (String name : List.of(
                "ineffable_hud_concept_base.png",
                "ineffable_hud_concept_lattice.png",
                "ineffable_hud_concept_inversion.png",
                "ineffable_hud_concept_contradiction.png",
                "ineffable_hud_concept_mana.png",
                "ineffable_hud_concept_mana_cap.png",
                "ineffable_hud_concept_paradox.png",
                "ineffable_hud_concept_xp.png"
        )) {
            BufferedImage image = ImageIO.read(TEXTURES.resolve(name).toFile());
            assertEquals(976, image.getWidth(), name);
            assertEquals(158, image.getHeight(), name);
            assertTrue(hasVisiblePixels(image), name);
        }

        BufferedImage badge = ImageIO.read(
                TEXTURES.resolve("ineffable_hud_concept_badge.png").toFile()
        );
        assertEquals(158, badge.getWidth());
        assertEquals(158, badge.getHeight());
        assertTrue(hasVisiblePixels(badge));

        BufferedImage base = ImageIO.read(
                TEXTURES.resolve("ineffable_hud_concept_base.png").toFile()
        );
        assertEquals(0, base.getRGB(0, 0) >>> 24);
        assertEquals(0, base.getRGB(975, 157) >>> 24);
        assertEquals(0, badge.getRGB(0, 0) >>> 24);
        assertEquals(0, badge.getRGB(157, 157) >>> 24);
    }

    @Test
    void channelGeometryMatchesTheConceptProportions() {
        assertEquals(80, IneffableHudConcept.CHANNEL_X);
        assertEquals(52, IneffableHudConcept.CHANNEL_Y);
        assertEquals(790, IneffableHudConcept.CHANNEL_WIDTH);
        assertEquals(54, IneffableHudConcept.CHANNEL_HEIGHT);
        assertEquals(
                80.0F / 976.0F,
                IneffableHudConcept.channelDisplayX(),
                0.0001F
        );
    }

    @Test
    void rendererUsesTheHighResolutionConceptGeometry() {
        assertEquals(153, IneffableHudRenderer.FRAME_WIDTH);
        assertEquals(25, IneffableHudRenderer.FRAME_HEIGHT);
        assertEquals(790, IneffableHudRenderer.CHANNEL_WIDTH);
        assertEquals(34, IneffableHudRenderer.FRAME_X);
        assertEquals(395, IneffableHudRenderer.manaPixels(50.0F, 100.0F));
        assertEquals(198, IneffableHudRenderer.paradoxPixels(25.0F, 100.0F));
    }

    @Test
    void manaCapTracksTheLiveRightEdge() {
        assertEquals(-1, IneffableHudConcept.manaCapX(0));
        assertEquals(470, IneffableHudConcept.manaCapX(395));
        assertEquals(865, IneffableHudConcept.manaCapX(790));
        assertEquals(865, IneffableHudConcept.manaCapX(999));
    }

    private static boolean hasVisiblePixels(BufferedImage image) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
