package com.aeappss.multiplayer.hitObjects;


public enum AnimationEnum {

    BALL("Ball"),
    ROCK("Rock"),
    BONE("Bone"),
    STICK("Stick"),
    FIGURE("Figure");

    private String displayName;

    AnimationEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
