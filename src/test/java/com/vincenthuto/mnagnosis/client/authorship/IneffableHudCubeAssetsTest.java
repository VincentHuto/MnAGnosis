package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IneffableHudCubeAssetsTest {

    private static final Path PARTICLES = Path.of(
            "src/main/resources/assets/mnagnosis/textures/particle"
    );

    @Test
    void hudCubesReuseTheWorldParticleTextures() throws IOException {
        for (String name : List.of(
                "ineffable_white_cube.png",
                "ineffable_black_cube.png"
        )) {
            BufferedImage image = ImageIO.read(PARTICLES.resolve(name).toFile());

            assertNotNull(image, name);
            assertEquals(16, image.getWidth(), name);
            assertEquals(16, image.getHeight(), name);
        }
    }
}
