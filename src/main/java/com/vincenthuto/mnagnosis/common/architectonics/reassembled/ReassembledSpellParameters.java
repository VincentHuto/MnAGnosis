package com.vincenthuto.mnagnosis.common.architectonics.reassembled;

import com.mna.api.spells.attributes.Attribute;
import com.mna.api.spells.base.IModifiedSpellPart;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.api.spells.parts.SpellEffect;

public final class ReassembledSpellParameters {
    private ReassembledSpellParameters() {
    }

    public static ReassembledParameters from(
            IModifiedSpellPart<SpellEffect> part,
            ISpellDefinition spell
    ) {
        return new ReassembledParameters(
                Math.round(part.getValue(Attribute.RANGE)),
                Math.round(part.getValue(Attribute.WIDTH)),
                Math.round(part.getValue(Attribute.HEIGHT)),
                Math.round(part.getValue(Attribute.DEPTH)),
                Math.round(part.getValue(Attribute.RADIUS)),
                Math.round(part.getValue(Attribute.DURATION) * 20.0F),
                false);
    }
}
