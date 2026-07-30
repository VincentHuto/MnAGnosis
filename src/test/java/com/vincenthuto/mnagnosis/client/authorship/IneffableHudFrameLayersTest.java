package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IneffableHudFrameLayersTest {

    private static final Path TEXTURES = Path.of(
            "src/main/resources/assets/mnagnosis/textures/mna"
    );
    private static final int DARK = 0xFF050505;

    @Test
    void splitLayersReconstructTheOriginalBasePixelForPixel()
            throws IOException {
        BufferedImage base = image("ineffable_hud_concept_base.png");
        BufferedImage backing = image(
                "ineffable_hud_concept_backing.png"
        );
        BufferedImage frame = image("ineffable_hud_concept_frame.png");

        assertEquals(976, backing.getWidth());
        assertEquals(158, backing.getHeight());
        assertEquals(976, frame.getWidth());
        assertEquals(158, frame.getHeight());

        int backingPixels = 0;
        int channelFramePixels = 0;
        for (int y = 0; y < base.getHeight(); y++) {
            for (int x = 0; x < base.getWidth(); x++) {
                int baseArgb = base.getRGB(x, y);
                int backingArgb = backing.getRGB(x, y);
                int frameArgb = frame.getRGB(x, y);
                boolean insideChannel = x >= 80 && x < 870
                        && y >= 52 && y < 106;

                if (insideChannel && baseArgb == DARK) {
                    assertEquals(baseArgb, backingArgb);
                    assertEquals(0, frameArgb >>> 24);
                    backingPixels++;
                } else {
                    assertEquals(0, backingArgb >>> 24);
                    assertEquals(baseArgb, frameArgb);
                    if (insideChannel && (frameArgb >>> 24) != 0) {
                        channelFramePixels++;
                    }
                }
            }
        }

        assertEquals(41_713, backingPixels);
        assertEquals(947, channelFramePixels);
    }

    private static BufferedImage image(String name) throws IOException {
        return ImageIO.read(TEXTURES.resolve(name).toFile());
    }
}
