package com.vincenthuto.mnagnosis.common.spell;

import com.mna.api.affinity.Affinity;
import com.mna.api.spells.SpellPartTags;
import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.attributes.AttributeValuePair;
import com.vincenthuto.mnagnosis.common.architectonics.reassembled.ReassembledPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentReassembledLandTest {
    @Test
    void onlyDownwardSolidImpactsEnterExcavationMode() {
        assertTrue(ComponentReassembledLand.excavationMode(
                new Vec3(0.0D, -0.75D, 1.0D),
                Direction.NORTH,
                true));
        assertTrue(ComponentReassembledLand.excavationMode(
                new Vec3(0.0D, -0.20D, 1.0D),
                Direction.UP,
                true));
        assertFalse(ComponentReassembledLand.excavationMode(
                new Vec3(0.0D, 0.0D, 1.0D),
                Direction.UP,
                true));
        assertFalse(ComponentReassembledLand.excavationMode(
                new Vec3(0.0D, -0.20D, 1.0D),
                Direction.NORTH,
                true));
        assertFalse(ComponentReassembledLand.excavationMode(
                new Vec3(0.0D, -1.0D, 0.0D),
                Direction.UP,
                false));
    }

    @Test
    void stairUsesExactImpactWhileOtherPatternsRemainFaceAdjacent() {
        BlockPos impact = new BlockPos(3, 7, 11);

        assertEquals(
                impact,
                ComponentReassembledLand.targetAnchor(
                        impact,
                        Direction.UP,
                        ReassembledPattern.STAIR));
        assertEquals(
                impact,
                ComponentReassembledLand.targetAnchor(
                        impact,
                        Direction.WEST,
                        ReassembledPattern.STAIR));
        assertEquals(
                impact.north(),
                ComponentReassembledLand.targetAnchor(
                        impact,
                        Direction.NORTH,
                        ReassembledPattern.WALL));
    }

    @Test
    void exposesTheFrozenTierSixUtilityContract() {
        ComponentReassembledLand component = new ComponentReassembledLand(
                ResourceLocation.fromNamespaceAndPath(
                        "mnagnosis",
                        "textures/spell/component/reassembled_land.png"));

        assertTrue(component.targetsBlocks());
        assertFalse(component.targetsEntities());
        assertEquals(Affinity.UNKNOWN, component.getAffinity());
        assertEquals(SpellPartTags.UTILITY, component.getUseTag());
        assertEquals(78.0F, component.initialComplexity());
        assertEquals(7000, component.requiredXPForRote());

        Map<Attribute, AttributeValuePair> attributes =
                Arrays.stream(ComponentReassembledLand.attributeContract())
                        .collect(
                        Collectors.toMap(
                                AttributeValuePair::getAttribute,
                                value -> value));
        assertAttribute(attributes, Attribute.RANGE, 12, 4, 24);
        assertAttribute(attributes, Attribute.WIDTH, 5, 1, 15);
        assertAttribute(attributes, Attribute.HEIGHT, 4, 1, 12);
        assertAttribute(attributes, Attribute.DEPTH, 5, 1, 15);
        assertAttribute(attributes, Attribute.RADIUS, 3, 2, 6);
        assertAttribute(attributes, Attribute.DURATION, 10, 2, 30);
        assertAttribute(attributes, Attribute.PRECISION, 0, 0, 1);
        assertEquals(
                InteractionHand.OFF_HAND,
                ComponentReassembledLand.latticeHand()
        );
    }

    private static void assertAttribute(
            Map<Attribute, AttributeValuePair> attributes,
            Attribute attribute,
            float expectedDefault,
            float expectedMinimum,
            float expectedMaximum
    ) {
        AttributeValuePair value = attributes.get(attribute);
        assertEquals(expectedDefault, value.getDefaultValue());
        assertEquals(expectedMinimum, value.getMinimum());
        assertEquals(expectedMaximum, value.getMaximum());
    }
}
