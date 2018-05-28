package com.aeappss.multiplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.aeappss.multiplayer.hitObjects.Animation;
import com.aeappss.multiplayer.hitObjects.Adapter;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.util.List;


public class SplashScreenActivity extends Activity implements
        DiscreteScrollView.ScrollStateChangeListener<Adapter.ViewHolder>,
        DiscreteScrollView.OnItemChangedListener<Adapter.ViewHolder>,
        View.OnClickListener{

    private List<Animation> forecasts;

    private DiscreteScrollView cityPicker;

    private static int SPLASH_TIME_OUT = 2500;
    Button button;
    Intent mainIntent;
    TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_splash_screen);
        Typeface myFont = Typeface.createFromAsset(getAssets(), "fonts/rocko.ttf");

        text = (TextView) findViewById(R.id.textView2);
        text.setTypeface(myFont);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onCurrentItemChanged(@Nullable Adapter.ViewHolder holder, int position) {

    }

    @Override
    public void onScrollStart(@NonNull Adapter.ViewHolder holder, int position) {
        holder.hideText();
    }

    @Override
    public void onScroll(
            float position,
            int currentIndex, int newIndex,
            @Nullable Adapter.ViewHolder currentHolder,
            @Nullable Adapter.ViewHolder newHolder) {
        Animation current = forecasts.get(currentIndex);
        if (newIndex >= 0 && newIndex < cityPicker.getAdapter().getItemCount()) {
            Animation next = forecasts.get(newIndex);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home:
                finish();
                break;
            case R.id.btn_transition_time:
                DiscreteScrollViewOptions.configureTransitionTime(cityPicker);
                break;
            case R.id.btn_smooth_scroll:
                DiscreteScrollViewOptions.smoothScrollToUserSelectedPosition(cityPicker, v);
                break;
        }
    }

    @Override
    public void onScrollEnd(@NonNull Adapter.ViewHolder holder, int position) {

    }
}
