package com.aeappss.multiplayer.hitObjects;
public class Animation {

    private final String name;
    private final int icon;
    private AnimationEnum animation;

    public Animation(String name, int icon, String temp, AnimationEnum animation) {
        this.name = "";
        this.icon = icon;
        this.animation = animation;
    }

    public Animation(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public AnimationEnum getAnimation() {
        return animation;
    }
}
