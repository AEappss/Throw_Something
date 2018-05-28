package com.aeappss.multiplayer.hitObjects;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

//import com.yarolegovich.discretescrollview.DiscreteScrollView;
//import com.yarolegovich.discretescrollview.sample.DiscreteScrollViewOptions;
//import com.yarolegovich.discretescrollview.sample.R;

import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.util.List;


public class AnimationActivity extends AppCompatActivity implements
        DiscreteScrollView.ScrollStateChangeListener<Adapter.ViewHolder>,
        DiscreteScrollView.OnItemChangedListener<Adapter.ViewHolder>,
        View.OnClickListener {

    private List<Animation> hitObjects;

    private RecyclerView recyclerView;
    private DiscreteScrollView picker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onCurrentItemChanged(@Nullable Adapter.ViewHolder holder, int position) {
        //viewHolder will never be null, because we never remove items from adapter's list
        if (holder != null) {
            recyclerView.setForecast(hitObjects.get(position));
            holder.showText();
        }

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
        Animation current = hitObjects.get(currentIndex);
        if (newIndex >= 0 && newIndex < picker.getAdapter().getItemCount()) {
            Animation next = hitObjects.get(newIndex);
            recyclerView.onScroll(1f - Math.abs(position), current, next);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onScrollEnd(@NonNull Adapter.ViewHolder holder, int position) {

    }
}
