package com.example.anybody.rewriteflexibleadapter.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by anybody on 2016/8/26.
 */
public class FlexibleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private final FlexibleAdapter _adapter;
    protected final OnListItemClickListener _clickListener;

    public FlexibleViewHolder(View itemView, FlexibleAdapter adapter, OnListItemClickListener clickListener) {
        super(itemView);
        _adapter = adapter;
        _clickListener = clickListener;
        this.itemView.setOnClickListener(this);
        this.itemView.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (_clickListener != null &&
                _clickListener.onListItemClick(getAdapterPosition())) {
            toggleActivation();
        }
    }


    protected void toggleActivation() {
        this.itemView.setActivated(_adapter.isSelected(getAdapterPosition()));
    }

    @Override
    public boolean onLongClick(View v) {
        if (_clickListener != null &&
                _clickListener.onListItemLongClick(getAdapterPosition())) {
            toggleActivation();
            return true;
        }
        return false;
    }

    public interface OnListItemClickListener{
        boolean onListItemClick(int position);
        boolean onListItemLongClick(int position);
    }

}
