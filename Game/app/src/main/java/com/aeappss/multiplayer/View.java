package com.aeappss.multiplayer;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.ArrayRes;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aeappss.multiplayer.hitObjects.Animation;
import com.aeappss.multiplayer.hitObjects.AnimationEnum;
import com.bumptech.glide.Glide;

public class View extends LinearLayout {

    private Paint gradientPaint;
    private int[] currentGradient;

    //private TextView weatherDescription;
    //private TextView weatherTemperature;
    private ImageView image;

    private ArgbEvaluator evaluator;

    public View(Context context) {
        super(context);
    }

    public View(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
        evaluator = new ArgbEvaluator();

        gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(false);

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        inflate(getContext(), R.layout.view_forecast, this);

        //weatherDescription = (TextView) findViewById(R.id.weather_description);
        image = (ImageView) findViewById(R.id.weather_image);
       // weatherTemperature = (TextView) findViewById(R.id.weather_temperature);
    }

    private void initGradient() {
        float centerX = getWidth() * 0.5f;
        Shader gradient = new LinearGradient(
                centerX, 0, centerX, getHeight(),
                currentGradient, null,
                Shader.TileMode.MIRROR);
        gradientPaint.setShader(gradient);
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
        canvas.drawRect(0, 0, getWidth(), getHeight(), gradientPaint);
        super.onDraw(canvas);
    }

    public void setForecast(Animation forecast) {
        AnimationEnum weather = forecast.getAnimation();
        currentGradient = weatherToGradient(weather);
        if (getWidth() != 0 && getHeight() != 0) {
            initGradient();
        }
        //weatherDescription.setText(weather.getDisplayName());
        //weatherTemperature.setText(forecast.getTemperature());
        Glide.with(getContext()).load(weatherToIcon(weather)).into(image);
        invalidate();

        image.animate()
                .scaleX(1f).scaleY(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300)
                .start();
    }

    public void onScroll(float fraction, Animation oldF, Animation newF) {
        image.setScaleX(fraction);
        image.setScaleY(fraction);
        currentGradient = mix(fraction,
                weatherToGradient(newF.getAnimation()),
                weatherToGradient(oldF.getAnimation()));
        initGradient();
        invalidate();
    }

    private int[] mix(float fraction, int[] c1, int[] c2) {
        return new int[]{
                (Integer) evaluator.evaluate(fraction, c1[0], c2[0]),
                (Integer) evaluator.evaluate(fraction, c1[1], c2[1]),
                (Integer) evaluator.evaluate(fraction, c1[2], c2[2])
        };
    }

    private int[] weatherToGradient(AnimationEnum weather) {
        switch (weather) {
            case BALL:
                return colors(R.array.gradientPeriodicClouds);
            case ROCK:
                return colors(R.array.gradientCloudy);
            case STICK:
                return colors(R.array.gradientMostlyCloudy);
            case BONE:
                return colors(R.array.gradientPartlyCloudy);
            case FIGURE:
                return colors(R.array.gradientClear);
            default:
                throw new IllegalArgumentException();
        }
    }

    private int weatherToIcon(AnimationEnum weather) {
        switch (weather) {
            case BALL:
                return R.drawable.periodic_clouds;
            case ROCK:
                return R.drawable.cloudy;
            case STICK:
                return R.drawable.mostly_cloudy;
            case BONE:
                return R.drawable.partly_cloudy;
            case FIGURE:
                return R.drawable.clear;
            default:
                throw new IllegalArgumentException();
        }
    }

    private int[] colors(@ArrayRes int res) {
        return getContext().getResources().getIntArray(res);
    }

}