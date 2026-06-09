package com.tailormade.tailor.data;

public enum MannequinPose {
    DEFAULT,
    ATTENTION,
    HANDS_ON_HIPS,
    ONE_ARM_UP,
    WALK;

    public MannequinPose next() {
        MannequinPose[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
