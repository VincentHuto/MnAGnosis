package com.vincenthuto.mnagnosis.client.authorship;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IneffableHudPerspectiveTest {

    private static final float EPSILON = 0.0001F;

    @Test
    void keepsTheLeftCenterPivotFixed() {
        IneffableHudPerspective.Point point =
                IneffableHudPerspective.project(14.0F, 18.0F, 0.0F);

        assertEquals(14.0F, point.x(), EPSILON);
        assertEquals(18.0F, point.y(), EPSILON);
        assertEquals(0.0F, point.z(), EPSILON);
    }

    @Test
    void sendsTheRightEdgeUpAndAwayFromThePlayer() {
        IneffableHudPerspective.Point right =
                IneffableHudPerspective.project(187.0F, 18.0F, 0.0F);

        assertEquals(180.73549F, right.x(), EPSILON);
        assertEquals(9.26176F, right.y(), EPSILON);
        assertEquals(-29.44031F, right.z(), EPSILON);
    }

    @Test
    void keepsTheAngledHudCompact() {
        List<IneffableHudPerspective.Point> corners = List.of(
                IneffableHudPerspective.project(14.0F, 4.0F, 0.0F),
                IneffableHudPerspective.project(187.0F, 4.0F, 0.0F),
                IneffableHudPerspective.project(14.0F, 38.0F, 0.0F),
                IneffableHudPerspective.project(187.0F, 38.0F, 0.0F)
        );
        float minimumX = corners.stream()
                .map(IneffableHudPerspective.Point::x)
                .min(Float::compare)
                .orElseThrow();
        float maximumX = corners.stream()
                .map(IneffableHudPerspective.Point::x)
                .max(Float::compare)
                .orElseThrow();
        float minimumY = corners.stream()
                .map(IneffableHudPerspective.Point::y)
                .min(Float::compare)
                .orElseThrow();
        float maximumY = corners.stream()
                .map(IneffableHudPerspective.Point::y)
                .max(Float::compare)
                .orElseThrow();

        assertTrue(maximumX - minimumX >= 160.0F);
        assertTrue(maximumX - minimumX <= 173.0F);
        assertTrue(maximumY - minimumY >= 30.0F);
        assertTrue(maximumY - minimumY <= 45.0F);
    }

    @Test
    void poseStackUsesTheSameProjectionOrder() {
        PoseStack pose = new PoseStack();
        IneffableHudPerspective.apply(pose);
        Vector4f actual = new Vector4f(187.0F, 18.0F, 0.0F, 1.0F);
        pose.last().pose().transform(actual);

        assertEquals(180.73549F, actual.x(), EPSILON);
        assertEquals(9.26176F, actual.y(), EPSILON);
        assertEquals(-29.44031F, actual.z(), EPSILON);
    }
}
