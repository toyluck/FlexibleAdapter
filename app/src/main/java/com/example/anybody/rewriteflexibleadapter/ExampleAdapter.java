package com.example.anybody.rewriteflexibleadapter;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.anybody.rewriteflexibleadapter.adapter.FlexibleAnimatorAdapter;
import com.example.anybody.rewriteflexibleadapter.adapter.FlexibleViewHolder;

import java.util.List;

/**
 * Created by anybody on 2016/8/26.
 */
public class ExampleAdapter extends FlexibleAnimatorAdapter<FlexibleViewHolder,Item> {

    private static final String TAG = ExampleAdapter.class.getSimpleName();
    public ExampleAdapter(Object listener, RecyclerView recyclerView) {
        super(DataService.getInstance().getItemsWithTag(TAG), listener, recyclerView);

    }

    @Override
    protected List<Animator> getAnimators(View view, int position, boolean isSelected) {
        return null;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }
}
