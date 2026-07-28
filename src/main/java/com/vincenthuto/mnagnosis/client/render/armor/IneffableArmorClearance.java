package com.vincenthuto.mnagnosis.client.render.armor;

public record IneffableArmorClearance(
        boolean helmet,
        boolean chest,
        boolean legs,
        boolean feet
) {

    static final float HELMET_OFFSET = 0.75F;
    static final float CHEST_OFFSET = 0.90F;
    static final float LEGS_OFFSET = 0.55F;
    static final float FEET_OFFSET = 0.35F;

    public static final IneffableArmorClearance NONE =
            new IneffableArmorClearance(false, false, false, false);

    public static IneffableArmorClearance from(
            boolean helmet,
            boolean chest,
            boolean legs,
            boolean feet
    ) {
        return helmet || chest || legs || feet
                ? new IneffableArmorClearance(helmet, chest, legs, feet)
                : NONE;
    }

    public float helmetOffset() {
        return this.helmet ? HELMET_OFFSET : 0.0F;
    }

    public float chestOffset() {
        return this.chest ? CHEST_OFFSET : 0.0F;
    }

    public float legsOffset() {
        return this.legs ? LEGS_OFFSET : 0.0F;
    }

    public float feetOffset() {
        return this.feet ? FEET_OFFSET : 0.0F;
    }

    public float lowerBodyOffset() {
        return Math.max(legsOffset(), feetOffset());
    }
}
