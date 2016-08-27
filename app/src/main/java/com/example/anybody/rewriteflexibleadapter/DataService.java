package com.example.anybody.rewriteflexibleadapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anybody on 2016/8/26.
 */
public class DataService {
    private static final int ITEMS = 1000;
    private static DataService _dataService;
    public static boolean userLearnedSelection=false;

    public static DataService getInstance() {
        if (_dataService == null) {
            _dataService = new DataService();
        }
        return _dataService;
    }

    DataService() {
        init();
    }

    private List<Item> _items = new ArrayList<>();

    private void init() {
        for (int i = 0; i < ITEMS; i++) {
            _items.add(newExampleItem(i));
        }
    }

    private Item newExampleItem(int i) {
        Item item = new Item();
        item.setId(i);
        item.setTitle("title = " + i);
        item.setSubTitle("subTitle = " + i);
        return item;
    }

    public List<Item> getItemsWithTag(String tag) {
        return new ArrayList<>(_items);
    }

    public Item removeId(int position) {
        return _items.remove(position);
    }
    public boolean removeItem(Item item){
        return _items.remove(item);
    }

    public void addItem(int position, Item item) {
        _items.add(position, item);
    }
    public void onDestroy(){
        _dataService=null;
    }
}

