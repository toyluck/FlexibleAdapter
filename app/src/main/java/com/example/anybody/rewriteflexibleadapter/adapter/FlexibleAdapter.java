package com.example.anybody.rewriteflexibleadapter.adapter;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by anybody on 2016/8/26.
 */
public abstract class FlexibleAdapter<VH extends RecyclerView.ViewHolder, T> extends SelectableAdapter {

    private static final String TAG = FlexibleAdapter.class.getSimpleName();
    private static final long UNDO_TIME = 5000L;
    protected List<T> _items;
    private Object _Lock = new Object();
    private OnUpdateListener _updateListener;
    private ArrayList<T> _DeletedItems;
    private ArrayList<Integer> _OriginalPositions;
    private String _searchText;
    private Handler _handler;

    public FlexibleAdapter(@NonNull List<T> items) {
        this(items, null);
    }

    public FlexibleAdapter(@NonNull List<T> items, Object listener) {
        this._items = items;
        if (listener instanceof OnUpdateListener) {
            _updateListener = (OnUpdateListener) listener;
            _updateListener.onUpdateEmptyView(this._items.size());
        }
    }

    public void updateDataSet() {
        updateDataSet(null);
    }

    /**
     * This method will refresh the entire DataSet content.<br/>
     * The parameter is useful to filter the type of the DataSet.<br/>
     * Pass null value in case not used.
     *
     * @param param A custom parameter to filter the type of the DataSet
     */
    public abstract void updateDataSet(String param);



    public interface OnUpdateListener {
        void onUpdateEmptyView(int size);
    }

    public void addItem(int position, T item) {
        if (position < 0) {
            Log.w(TAG, "addItem: Cannot addItem on negative position");
            return;
        }
        if (position < _items.size()) {
            Log.w(TAG, "addItem: notifyItemInserted on position");
            synchronized (_Lock) {

                _items.add(position, item);
            }
        } else {
            Log.d(TAG, "addItem: notifyItemInserted on last position");
            synchronized (_Lock) {
                _items.add(item);
                position = _items.size();
            }
        }
        notifyItemInserted(position);
        if (_updateListener != null) _updateListener.onUpdateEmptyView(_items.size());
    }


    public void removeItem(int position) {
        if (position < 0) {
            Log.d(TAG, "removeItem: position on the negative");
            return;
        }
        if (position < _items.size()) {
            if (DEBUG) Log.d(TAG, "removeItem: notifyItemRemoved on position" + position);
            synchronized (_Lock) {
                saveDeletedItem(position, _items.remove(position));
            }
            notifyItemRemoved(position);
            updateEmptyView(_items.size());
        } else {
            Log.w(TAG, "removeItem: position OutOfBound!!");
        }
    }

    public void removeItems(List<Integer> items) {
        Log.d(TAG, "removeItems: reverse Sorting positions");
        Collections.sort(items, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        while (!items.isEmpty()) {
            if (items.size() == 1) {
                removeItem(items.get(0));
                items.remove(0);
            } else {
                int count = 1;
                while (items.size() > count && items.get(count).equals(items.get(count - 1) - 1))
                {
                    ++count;
                }
                if (count == 1) {
                    removeItem(items.get(0));
                } else {
                    removeRange(items.get(count - 1), count);
                }
                for (int i = 0; i < count; i++) {
                    items.remove(0);
                }
            }

        }
        if (DEBUG) Log.d(TAG, "removeItems current items" + getSelectedItems());


    }

    private void removeRange(int  positionStart, int count) {
        if (DEBUG) Log.d(TAG, "removeRange: positionStart :" + positionStart + "itemCount : " + count);
        for (int i = 0; i < count; ++i) {
            synchronized (_Lock) {
                saveDeletedItem(positionStart, _items.remove(positionStart));
            }
        }
        if (DEBUG) Log.d(TAG, "removeRange: notifyItemRangeRemoved");
        notifyItemRangeRemoved(positionStart, count);
        updateEmptyView(_items.size());

    }

    private void saveDeletedItem(int position, T removedItem) {
        if (_DeletedItems == null) {
            _DeletedItems = new ArrayList<T>();
            _OriginalPositions = new ArrayList<>();
        }
        Log.d(TAG, "saveDeletedItem: "
                + removedItem + "position : " + position);
        _DeletedItems.add(removedItem);
        _OriginalPositions.add(position);
    }


    public ArrayList<T> getDeletedItems() {
        return _DeletedItems;
    }

    public ArrayList<Integer> getOriginalPositions() {
        return _OriginalPositions;
    }


    public int getPositionForItem(T item) {
        return _items != null && _items.size() > 0 ? _items.indexOf(item) : -1;
    }

    /*Filter Methods*/

    public String getSearchText() {
        return _searchText;
    }

    public boolean hasSearchText() {
        return !TextUtils.isEmpty(getSearchText());
    }

    public void setSearchText(String searchText) {
        if (searchText != null) {
            _searchText = searchText.trim().toLowerCase(Locale.getDefault());
        } else {
            _searchText = "";
        }
    }

    public void restoreDeleteItems() {
        for (int i = _OriginalPositions.size() - 1; i >= 0; i--) {
            T item = _DeletedItems.get(i);
            Log.d(TAG, "restoreDeleteItem: " + item + " on position :" + i);
            if (hasSearchText() && !filterObject(item, getSearchText())) {
                continue;
            }
            addItem(_OriginalPositions.get(i), item);
        }
        emptybin();
    }

    private void emptybin() {
        if (_DeletedItems != null) {
            _DeletedItems.clear();
            _OriginalPositions.clear();
        }
    }

    /**
     * @param item      row
     * @param constants 要检查的字段
     * @return 检查是否包含该字段
     */
    private boolean filterObject(T item, String constants) {
        final String valueText = item.toString().trim().toLowerCase(Locale.getDefault());
        if (valueText.startsWith(constants)) {
            return true;
        } else {
            String[] values = valueText.split(" ");
            for (String value : values) {
                if (value.contains(constants)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void startUndoTimer(OnDeleteCompeleteListener listener) {
        startUndoTimer(0, listener);
    }

    public void startUndoTimer(int timeOut, final OnDeleteCompeleteListener listener) {
        _handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (listener != null) listener.confirmDeleted();
                emptybin();
                return true;
            }
        });
        _handler.sendMessageDelayed(Message.obtain(_handler), timeOut > 0 ? timeOut : UNDO_TIME);
    }

    public void stopUndoTimer() {
        if (_handler != null) {
            _handler.removeCallbacksAndMessages(null);
            _handler = null;
        }
    }

    public synchronized void filterItems(@NonNull List<T> unfilterItems) {
        if (hasSearchText()) {
            _items = new ArrayList<>();
            int newOriginalPosition = -1, oldOriginalPosition = -1;
            for (T unfilterItem : unfilterItems) {
                if (filterObject(unfilterItem, getSearchText())) {
                    if (_DeletedItems != null && _DeletedItems.contains(unfilterItem)) {
                        int index = _DeletedItems.indexOf(unfilterItem);
                        //重新计算在 OriginalPositions中的数据,假如数据已经被删除先
                        if (_OriginalPositions.get(index) != oldOriginalPosition) {
                            newOriginalPosition++;
                            oldOriginalPosition = _OriginalPositions.get(index);
                        }
                        _OriginalPositions.set(index, newOriginalPosition + _items.size());
                    }
                    _items.add(unfilterItem);
                }
            }
        } else {
            /*如果没有filter存在,则过滤掉已经删除的items*/
            _items = unfilterItems;
            if (_DeletedItems != null && _DeletedItems.size() > 0) {
                _OriginalPositions = new ArrayList<>(_DeletedItems.size());
                for (T deletedItem : _DeletedItems) {
                    int position = _items.indexOf(deletedItem);
                    _OriginalPositions.add(position);
                }
                _items.removeAll(_DeletedItems);
            }

        }

    }

    public interface OnDeleteCompeleteListener {
        void confirmDeleted();
    }


    protected void updateEmptyView(int count) {
        if (_updateListener != null) _updateListener.onUpdateEmptyView(count);
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    @Override
    public int getItemCount() {
        return _items != null ? _items.size() : 0;
    }
}
