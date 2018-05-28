package com.aeappss.multiplayer.hitObjects;

import com.aeappss.multiplayer.R;

import java.util.Arrays;
import java.util.List;


public class AnimationStation {


    public static AnimationStation get() {
        return new AnimationStation();
    }

    private AnimationStation() {
    }

    public List<Animation> getForecasts() {
        return Arrays.asList(
                new Animation("", R.drawable.bone1, "16", AnimationEnum.BONE),
                new Animation("", R.drawable.ball, "14", AnimationEnum.BALL),
                new Animation("", R.drawable.brick, "16", AnimationEnum.FIGURE),
                new Animation("", R.drawable.stick, "14", AnimationEnum.STICK),
                //new Animation("", R.drawable.fish, "14", AnimationEnum.FIGURE),
                new Animation("", R.drawable.rock, "9", AnimationEnum.ROCK));

    }
}
