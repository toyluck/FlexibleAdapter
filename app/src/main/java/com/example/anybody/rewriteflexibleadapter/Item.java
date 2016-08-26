package com.example.anybody.rewriteflexibleadapter;

import android.text.TextUtils;
import android.view.TextureView;

/**
 * Created by anybody on 2016/8/26.
 *
 */
public class Item {
    private int _id;
    private String _title;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Item){
            Item item = (Item) obj;
            return item.getId()==this.getId();
        }
        return super.equals(obj);
    }

    public String getSubTitle() {
        return _subTitle;
    }

    public void setSubTitle(String subTitle) {
        _subTitle = subTitle;
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        _title = title;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    private String _subTitle;
}
