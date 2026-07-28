package com.vincenthuto.mnagnosis.mixin.core;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.Shape;
import com.mna.api.spells.targeting.SpellSource;
import com.mna.api.spells.targeting.SpellTarget;
import com.mna.spells.shapes.ShapeRaytrace;
import com.vincenthuto.mnagnosis.common.spell.livingland.LivingLandAimedTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ShapeRaytrace.class, remap = false)
public abstract class ShapeRaytraceLivingLandMixin {

    @Inject(method = "Target", at = @At("RETURN"), cancellable = true)
    private void mnagnosis$placeLivingLandAtAimedMiss(
            SpellSource source,
            Level level,
            IModifiedSpellPart<Shape> modifiedShape,
            ISpellDefinition spell,
            CallbackInfoReturnable<List<SpellTarget>> callback
    ) {
        List<SpellTarget> targets = callback.getReturnValue();
        if (source == null || targets == null || targets.size() != 1
                || targets.get(0) != SpellTarget.NONE
                || !LivingLandAimedTargeting.shouldCreateFallback(spell)) {
            return;
        }
        Vec3 forward = source.getForward();
        Vec3 endpoint = LivingLandAimedTargeting.fallbackPosition(
                source.getOrigin(), forward,
                modifiedShape.getValue(Attribute.RANGE));
        Direction face = Direction.getNearest(
                (float) -forward.x, (float) -forward.y, (float) -forward.z);
        callback.setReturnValue(List.of(
                new SpellTarget(BlockPos.containing(endpoint), face)
                        .doNotOffsetFace()));
    }
}
