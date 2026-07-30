package com.vincenthuto.mnagnosis.client.authorship;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableHudCubeLayoutTest {

    private static final float EPSILON = 0.0001F;

    @Test
    void mapsFourConceptNodesIntoTheCompactHud() {
        List<IneffableHudCubeLayout.Anchor> anchors =
                IneffableHudCubeLayout.anchors();

        assertEquals(4, anchors.size());
        assertAnchor(anchors.get(0), 171.5F, 13.5F);
        assertAnchor(anchors.get(1), 206.5F, 20.5F);
        assertAnchor(anchors.get(2), 782.5F, 144.5F);
        assertAnchor(anchors.get(3), 821.5F, 134.5F);
    }

    @Test
    void alternatesTheExistingWhiteAndBlackCubeStyles() {
        assertEquals(
                IneffableHudCubeLayout.TextureVariant.WHITE,
                IneffableHudCubeLayout.anchors().get(0).texture()
        );
        assertEquals(
                IneffableHudCubeLayout.TextureVariant.BLACK,
                IneffableHudCubeLayout.anchors().get(1).texture()
        );
        assertEquals(
                IneffableHudCubeLayout.TextureVariant.WHITE,
                IneffableHudCubeLayout.anchors().get(2).texture()
        );
        assertEquals(
                IneffableHudCubeLayout.TextureVariant.BLACK,
                IneffableHudCubeLayout.anchors().get(3).texture()
        );
    }

    @Test
    void animationIsDeterministicAndDistinctPerEmitter() {
        float time = 147.25F;
        Set<IneffableHudCubeLayout.Sample> samples = new HashSet<>();

        for (int index = 0; index < 4; index++) {
            IneffableHudCubeLayout.Sample first =
                    IneffableHudCubeLayout.sample(index, time);
            IneffableHudCubeLayout.Sample second =
                    IneffableHudCubeLayout.sample(index, time);
            assertEquals(first, second);
            samples.add(first);
        }

        assertEquals(4, samples.size());
        assertNotEquals(
                IneffableHudCubeLayout.sample(0, time).rotationX(),
                IneffableHudCubeLayout.sample(1, time).rotationX()
        );
        assertNotEquals(
                IneffableHudCubeLayout.sample(0, time).rotationY(),
                IneffableHudCubeLayout.sample(1, time).rotationY()
        );
        assertNotEquals(
                IneffableHudCubeLayout.sample(0, time).rotationZ(),
                IneffableHudCubeLayout.sample(1, time).rotationZ()
        );
    }

    @Test
    void motionStaysBoundedAroundEachCircuitNode() {
        for (int tick = 0; tick <= 720; tick++) {
            float time = tick * 0.25F;
            for (int index = 0; index < 4; index++) {
                IneffableHudCubeLayout.Anchor anchor =
                        IneffableHudCubeLayout.anchors().get(index);
                IneffableHudCubeLayout.Sample sample =
                        IneffableHudCubeLayout.sample(index, time);

                assertTrue(Math.abs(sample.x() - anchor.displayX()) <= 1.01F);
                assertTrue(Math.abs(sample.y() - anchor.displayY()) <= 1.01F);
                assertTrue(sample.halfSize() >= 1.5F);
                assertTrue(sample.halfSize() <= 2.5F);
                assertTrue(sample.alpha() >= 0.65F);
                assertTrue(sample.alpha() <= 1.0F);
            }
        }
    }

    @Test
    void partialTickContributesToAnimationTime() {
        assertEquals(
                80.625F,
                IneffableHudCubeLayout.animationTime(80L, 0.625F),
                EPSILON
        );
    }

    private static void assertAnchor(
            IneffableHudCubeLayout.Anchor anchor,
            float sourceX,
            float sourceY
    ) {
        assertEquals(sourceX, anchor.sourceX(), EPSILON);
        assertEquals(sourceY, anchor.sourceY(), EPSILON);
        assertEquals(
                sourceX * IneffableHudConcept.DISPLAY_WIDTH
                        / IneffableHudConcept.SOURCE_WIDTH,
                anchor.displayX(),
                EPSILON
        );
        assertEquals(
                sourceY * IneffableHudConcept.DISPLAY_HEIGHT
                        / IneffableHudConcept.SOURCE_HEIGHT,
                anchor.displayY(),
                EPSILON
        );
    }
}
