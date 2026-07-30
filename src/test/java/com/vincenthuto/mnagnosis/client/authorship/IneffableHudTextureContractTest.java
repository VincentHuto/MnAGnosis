package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableHudTextureContractTest {

    private static final Path ATLAS = Path.of(
            "src/main/resources/assets/mnagnosis/textures/mna/"
                    + "ineffable_resource_bars.png"
    );
    private static final Set<Integer> PALETTE = Set.of(
            0x00000000,
            0xFF050505,
            0xFFF7F7F7,
            0xFF898989,
            0xFF00B8D4
    );

    @Test
    void atlasContainsEveryRuntimeLayer() throws IOException {
        BufferedImage atlas = ImageIO.read(ATLAS.toFile());

        assertEquals(256, atlas.getWidth());
        assertEquals(256, atlas.getHeight());
        for (IneffableHudAtlas.Sprite sprite : IneffableHudAtlas.ALL_SPRITES) {
            assertTrue(
                    hasOpaquePixel(atlas, sprite),
                    () -> "empty runtime sprite " + sprite
            );
        }
    }

    @Test
    void atlasUsesOnlyTheApprovedHardEdgedPalette() throws IOException {
        BufferedImage atlas = ImageIO.read(ATLAS.toFile());

        for (int y = 0; y < atlas.getHeight(); y++) {
            for (int x = 0; x < atlas.getWidth(); x++) {
                int argb = atlas.getRGB(x, y);
                assertTrue(
                        PALETTE.contains(argb),
                        "unexpected color 0x%08X at %d,%d"
                                .formatted(argb, x, y)
                );
            }
        }
    }

    private static boolean hasOpaquePixel(
            BufferedImage atlas,
            IneffableHudAtlas.Sprite sprite
    ) {
        for (int y = sprite.v(); y < sprite.bottom(); y++) {
            for (int x = sprite.u(); x < sprite.right(); x++) {
                if ((atlas.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
