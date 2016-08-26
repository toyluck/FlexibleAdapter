package com.example.anybody.rewriteflexibleadapter.adapter;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by anybody on 2016/8/25.
 *
 */
public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder,T>  extends RecyclerView.Adapter<VH> {
    private static final String TAG = SelectableAdapter.class.getSimpleName();

    protected static boolean DEBUG=false;
    /**
     * 记录选中的位置 position
     */
    private ArrayList<Integer> _selectedItems;
    private  int _model;

    public  void enablelog(boolean enable){
        DEBUG=enable;
    }

    @interface MODELS{

        public static final int MODE_SINGLE=1;
        public static final int MODE_MULTI=2;
    }

    public SelectableAdapter() {
        this._selectedItems = new ArrayList<>();
        this._model = MODELS.MODE_SINGLE;

    }

    public void setModel(@MODELS int _model) {
        this._model = _model;
    }

    /**
     * 检查是否已勾选
     * @param position
     * @return
     */
    public boolean isSelected(int position){
        return _selectedItems.contains(position);
    }


    public void togglePosition(int position){
        togglePosition(position,false);
    }

    /**
     *
     * @param position
     * @param invalidate 强制row刷新并重新绑定item
     */
    private void togglePosition(int position,boolean invalidate){
       if (position<0)return;
        if (_model==MODELS.MODE_SINGLE)clearSelection();
        Integer index = _selectedItems.get(position);
        if(index!=-1){
            if(DEBUG) Log.d(TAG, "togglePosition: remove selection position " +position);
            _selectedItems.remove(index);
        }else {
            if(DEBUG) Log.d(TAG, "togglePosition: add selection position " +position);
            _selectedItems.add(position);
        }
        if (invalidate){
            if (DEBUG) Log.v(TAG, "toggleSelection notifyItemChanged on position " + position);
            notifyItemChanged(position);
        }
        if (DEBUG) Log.v(TAG, "toggleSelection current selection " + _selectedItems);
    }
     private void clearSelection(){
         Iterator<Integer> iterator = _selectedItems.iterator();
         while (iterator.hasNext()){
             int i=iterator.next();
               iterator.remove();
             if(DEBUG) Log.d(TAG, "clearSelection: notifyItemChanged on position :" +i);
             notifyItemChanged(i);
         }
     }


    public void selectAll(){
      selectAll(-1);
    }

    public void selectAll(int skipViewType){
        if (DEBUG) Log.d(TAG, "selectAll: ");
        _selectedItems=new ArrayList<>(getItemCount());
        for (int i = 0; i < getItemCount(); i++) {
            if (getItemViewType(i)==skipViewType)continue;
            _selectedItems.add(i);
            Log.d(TAG, "selectAll: notifyItemChanged on position" + i);
        notifyItemChanged(i
        );
        }
    }

    public ArrayList<Integer> getSelectedItems() {
        return _selectedItems;
    }
    public int getSelectedCount(){
        return _selectedItems.size();
    }
    public void onSaveInstanceState(Bundle outState){
        outState.putIntegerArrayList(TAG,getSelectedItems());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        _selectedItems=savedInstanceState.getIntegerArrayList(TAG);
    }

}
