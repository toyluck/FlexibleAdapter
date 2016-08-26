package com.example.anybody.rewriteflexibleadapter.fastscroller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.anybody.rewriteflexibleadapter.R;

/**
 * Created by anybody on 2016/8/26.
 *
 */
public class FastScroller extends FrameLayout {
    public static final long BUBBLE_DURATION = 3000l;
    private static final int TRACK_SNAP_RANGE = 5;
    private boolean _isInitialized;
    private TextView _bubble;
    private View _handler;
    private int _height;
    private RecyclerView _recyclerView;
    private RecyclerView.OnScrollListener _onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            recaculPosition();
        }
    };
    private ObjectAnimator _objectAnimator;

    public FastScroller(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (_isInitialized) return;
        _isInitialized = true;

        setClipChildren(false);
    }


    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setViewsToUser(@LayoutRes int resId) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(resId, this, true);
        _bubble = (TextView) findViewById(R.id.fast_scroller_bubble);
        if (_bubble != null) _bubble.setVisibility(VISIBLE);
        _handler = findViewById(R.id.fast_scroller_handle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        _height = h;

    }


    public void setRecyclerView(RecyclerView view) {
        _recyclerView = view;
        _recyclerView.addOnScrollListener(_onScrollListener);
        _recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                _recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                recaculPosition();
                return true;
            }
        });
    }

    private void recaculPosition() {
        int verticalScrollOffset = _recyclerView.computeVerticalScrollOffset();
        int verticalScrollRange = _recyclerView.computeVerticalScrollRange();
        //计算出此时手指在ScrollerBar上的位置
        float propotion = (float) verticalScrollOffset / (float) (verticalScrollRange - _height);
        setBubbleAndHandlePosition(propotion);
    }


    private void setBubbleAndHandlePosition(float propotion) {
        int handlerHeight = _handler.getHeight();
        //根据propotion 计算中心位置
        _handler.setY(getValueInRange(0, _height - handlerHeight, (int) (propotion - handlerHeight / 2)));
        if (_bubble != null) {
            _bubble.setY(getValueInRange(0, _height - _bubble.getHeight() - handlerHeight / 2, (int) (propotion - _bubble.getHeight())));

        }
    }

    private int getValueInRange(int min, int max, int value) {
        int val = Math.max(min, value);
        return Math.min(val, max);
    }

    public void showBubble() {
        if (_bubble != null) {
            //添加一个 显露动画
            _bubble.setVisibility(VISIBLE);
            if (_objectAnimator != null) {
                _objectAnimator.cancel();
            }
            _objectAnimator = ObjectAnimator.ofFloat(_bubble, "alpha", 0f, 1f).setDuration(BUBBLE_DURATION);
            _objectAnimator.start();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                setBubbleAndHandlePosition(y);
                setRecyclerViewPosition(y);
                break;
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < _handler.getX() - ViewCompat.getPaddingStart(_handler))
                    return false;
                if (_objectAnimator != null) _objectAnimator.cancel();
                if (_bubble != null && _bubble.getVisibility() == INVISIBLE) showBubble();
                _handler.setSelected(true);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                _handler.setSelected(false);
                hideBubble();
                break;

        }
        return super.onTouchEvent(event);
    }

    private void setRecyclerViewPosition(float y) {
        if (_recyclerView != null) {
            int itemCount = _recyclerView.getAdapter().getItemCount();
            float propotion;
            if (_handler.getY() == 0) {
                propotion = 0f;
            } else if (_handler.getY() + _handler.getHeight() >= _height - TRACK_SNAP_RANGE) {
                propotion = 1f;
            } else {
                propotion = y / (float) _height;
            }

            int targetPos = getValueInRange(0, itemCount - 1, (int) (propotion * itemCount));
            LinearLayoutManager layoutManager = (LinearLayoutManager) _recyclerView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(targetPos, 0);
            BubbleTextGetter textGetter = (BubbleTextGetter) _recyclerView.getAdapter();
            if (_bubble != null)
                _bubble.setText(textGetter.getTextToShowInBubble(targetPos));


        }
    }

    public interface BubbleTextGetter {
        String getTextToShowInBubble(int posiion);
    }

    public void hideBubble() {
        if (_bubble != null) {
            //添加一个 显露动画
            _bubble.setVisibility(VISIBLE);
            if (_objectAnimator != null) {
                _objectAnimator.cancel();
            }
            _objectAnimator = ObjectAnimator.ofFloat(_bubble, "alpha", 1f, 0f).setDuration(BUBBLE_DURATION);
            _objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    _bubble.setVisibility(INVISIBLE);
                    _objectAnimator = null;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    _bubble.setVisibility(INVISIBLE);
                    _objectAnimator = null;
                }
            });
            _objectAnimator.start();
        }
    }
}
