package com.aeappss.multiplayer.hitObjects;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
//import com.yarolegovich.discretescrollview.sample.R;
import com.aeappss.multiplayer.R;


public class RecyclerView extends LinearLayout {

   // private Paint gradientPaint;
    private int[] currentGradient;

    public static ImageView image;

    private ArgbEvaluator evaluator;

    public RecyclerView(Context context) {
        super(context);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RecyclerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        evaluator = new ArgbEvaluator();

        //gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(true);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        inflate(getContext(), R.layout.view_forecast, this);


        image = (ImageView) findViewById(R.id.weather_image);

    }

    private void initGradient() {
        float centerX = getWidth() * 0.5f;
        Shader gradient = new LinearGradient(
                centerX, 0, centerX, getHeight(),
                currentGradient, null,
                Shader.TileMode.MIRROR);
        //gradientPaint.setShader(gradient);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (currentGradient != null) {
            initGradient();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

    }

    public void setForecast(Animation hitObj) {
        AnimationEnum animation = hitObj.getAnimation();
        currentGradient = hitObjToGradient(animation);
        if (getWidth() != 0 && getHeight() != 0) {
            initGradient();
        }

        Glide.with(getContext()).load(hitImgToIcon(animation)).into(image);
        invalidate();

        image.animate()
                .scaleX(1f).scaleY(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300)
                .start();

    }

    public void onScroll(float fraction, Animation oldF, Animation newF) {

    }


    public static  void  setScale(){
        image.setVisibility(VISIBLE);
        ScaleAnimation scaleAnimation = new ScaleAnimation(1f,0f,1f,0f, image.getWidth() / 2.0f, image.getHeight() / 2.0f);
        scaleAnimation.setDuration(3000);
        image.startAnimation(scaleAnimation);

        scaleAnimation.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            public void onAnimationEnd(android.view.animation.Animation animation) {
                Log.d("ScaleActivity", "Scale started...");
            }

            public void onAnimationRepeat(android.view.animation.Animation animation) {

            }

            public void onAnimationStart(android.view.animation.Animation animation) {
                Log.d("ScaleActivity", "Scale ended...");
            }
        });
        //weatherImage.setVisibility(INVISIBLE);
    }


    private int[] mix(float fraction, int[] c1, int[] c2) {
        return new int[]{
                (Integer) evaluator.evaluate(fraction, c1[0], c2[0]),
                (Integer) evaluator.evaluate(fraction, c1[1], c2[1]),
                (Integer) evaluator.evaluate(fraction, c1[2], c2[2])
        };
    }

    private int[] hitObjToGradient(AnimationEnum hitObject) {
        switch (hitObject) {
            case BALL:
                return colors(R.array.gradientPeriodicClouds);
            case ROCK:
                return colors(R.array.gradientCloudy);
            case STICK:
                return colors(R.array.gradientPartlyCloudy);
            case FIGURE:
                return colors(R.array.gradientClear);
            default:
                throw new IllegalArgumentException();
        }

    }

    private int hitImgToIcon(AnimationEnum hitObject) {
        switch (hitObject) {
            case BALL:
                return R.drawable.ball;
            case ROCK:
                return R.drawable.rock;
            case STICK:
                return R.drawable.stick;
            case FIGURE:
                return R.drawable.brick;
            default:
                throw new IllegalArgumentException();
        }
    }

    private int[] colors(@ArrayRes int res) {
        return getContext().getResources().getIntArray(res);
    }

}
