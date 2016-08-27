package com.example.anybody.rewriteflexibleadapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;

/**
 * Created by anybody on 2016/8/27.
 */
public class Utils {

    public static  boolean hasLollipop(){
        return Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP;
    }
    private static int _colorAccent=-1;
    public static  int getColorAccent(Context context){
        if (_colorAccent<0){
            int accentAttr = hasLollipop() ? android.R.attr.colorAccent : R.attr.colorAccent;
            TypedArray ta=context.getTheme().obtainStyledAttributes(new int[]{accentAttr});
            _colorAccent=ta.getColor(0,0xff009688);
        }
        return _colorAccent;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT>= Build.VERSION_CODES.M;
    }
}
