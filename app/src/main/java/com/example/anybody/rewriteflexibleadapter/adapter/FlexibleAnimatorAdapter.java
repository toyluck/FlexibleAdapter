package com.example.anybody.rewriteflexibleadapter.adapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.EnumSet;
import java.util.List;

/**
 * Created by anybody on 2016/8/26.
 *
 */
public abstract class FlexibleAnimatorAdapter<VH extends RecyclerView.ViewHolder, T> extends FlexibleAdapter<VH, T> {

    protected RecyclerView _recyclerView;
    //记录最后一个动画生效的item的位置
    private int _lastAnimatedPosition = -1;

    // 动画延迟？                 开启动画
    private boolean _isBackwardEnabled = true, _shouldAnimate = true;

    //记录使用的动画
    private EnumSet<AnimatorEnum> _animatorUsed = EnumSet.noneOf(AnimatorEnum.class);
    private long _InitialDelay = 0l;
    private long _StepDela = 100l;
    private long _Duration = 300l;
    private TimeInterpolator _Interpolator = new LinearInterpolator();

    private enum AnimatorEnum {
        ALPHA, SLIDE_IN_LEFT, SLIDE_IN_RIGHT, SLIDE_IN_BOTTOM, SCALE
    }

    public FlexibleAnimatorAdapter(@NonNull List<T> items, @NonNull RecyclerView recyclerView) {
        this(items, null, recyclerView);
    }

    public FlexibleAnimatorAdapter(@NonNull List<T> items, Object listener, RecyclerView recyclerView) {
        super(items, listener);

        _recyclerView = recyclerView;
        if (_recyclerView == null) {
            throw new IllegalArgumentException("recyclerView can't be null");
        }
    }

    public RecyclerView getRecyclerView() {
        return _recyclerView;
    }

    protected final void animateView(View view, int position, boolean isSelected) {
        if (_shouldAnimate && (!_isBackwardEnabled || position > _lastAnimatedPosition)) {
            //Retrieve user animators
            List<Animator> animators = getAnimators(view, position, isSelected);

            if (!_animatorUsed.contains(AnimatorEnum.ALPHA)) {
                addAlphaAnimator(animators, view, 0f);
            }

            _animatorUsed.clear();
            ViewCompat.setAlpha(view, 0);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);
            animatorSet.setStartDelay(_InitialDelay += _StepDela);
            animatorSet.setDuration(_Duration);
            animatorSet.setInterpolator(_Interpolator);
            animatorSet.start();
            _lastAnimatedPosition = position;
        }

        // 当界面满时，停止动画
        if (position > getVisibleItems()) {
            _InitialDelay = _StepDela = 0L;
        }
    }

    private int getVisibleItems() {
        RecyclerView.LayoutManager layoutManager = _recyclerView.getLayoutManager();
        int firstPosition = 0;
        int lastPosition = 0;

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
        firstPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        lastPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
        if (_lastAnimatedPosition > lastPosition) {
            lastPosition = _lastAnimatedPosition;

        } else if (layoutManager instanceof GridLayoutManager) {
            return lastPosition - firstPosition + 1;
        }
        return lastPosition - firstPosition - 1;
    }

    //添加 透明动画
    public void addAlphaAnimator(@NonNull List<Animator> animators, @NonNull View view, @FloatRange(from = 0, to = 1) float v) {
        if (_animatorUsed.contains(AnimatorEnum.ALPHA)) return;
        animators.add(ObjectAnimator.ofFloat(view, "alpha", v, 1f));
        _animatorUsed.add(AnimatorEnum.ALPHA);
    }

    public void addSlideInFromLeftAnimator(@NonNull List<Animator> animators,@NonNull View itemview,
                                           @FloatRange(from = 0.5,to = 1.0)float percent){
        if (_animatorUsed.contains(AnimatorEnum.SLIDE_IN_LEFT)||_animatorUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)
                ||_animatorUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT)
        )return;
        animators.add(ObjectAnimator.ofFloat(itemview, "translationX",-_recyclerView.getLayoutManager().getWidth()*percent,0));
        _animatorUsed.add(AnimatorEnum.SLIDE_IN_LEFT);
    }
    public void addSlideInFromRightAnimator(@NonNull List<Animator> animators,@NonNull View itemview,
                                           @FloatRange(from = 0.5,to = 1.0)float percent){
        if (_animatorUsed.contains(AnimatorEnum.SLIDE_IN_LEFT)||_animatorUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)
                ||_animatorUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT)
                )return;
        animators.add(ObjectAnimator.ofFloat(itemview, "translationX",_recyclerView.getLayoutManager().getWidth()*percent,0));
        _animatorUsed.add(AnimatorEnum.SLIDE_IN_RIGHT);
    }
    public void addSlideInFromBottomAnimator(@NonNull List<Animator> animators,@NonNull View itemview){
        if (_animatorUsed.contains(AnimatorEnum.SLIDE_IN_LEFT)||_animatorUsed.contains(AnimatorEnum.SLIDE_IN_BOTTOM)
                ||_animatorUsed.contains(AnimatorEnum.SLIDE_IN_RIGHT)
                )return;
        animators.add(ObjectAnimator.ofFloat(itemview, "translationY",_recyclerView.getMeasuredHeight()>>1,0));
        _animatorUsed.add(AnimatorEnum.SLIDE_IN_BOTTOM);
    }
    public void addScaleInAnimator(@NonNull List<Animator> animators,@NonNull View itemview,@FloatRange
            (from = 0.0,to = 1.0)float scaleFrom){
        if (_animatorUsed.contains(AnimatorEnum.SCALE))return;
        animators.add(ObjectAnimator.ofFloat(itemview,"scaleX",scaleFrom,1f));
        animators.add(ObjectAnimator.ofFloat(itemview,"scaleY",scaleFrom,1f));
        _animatorUsed.add(AnimatorEnum.SCALE);
    }

    public void setInitialDelay(long initialDelay) {
        _InitialDelay = initialDelay;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        _Interpolator = interpolator;
    }

    public void setShouldAnimate(boolean shouldAnimate) {
        _shouldAnimate = shouldAnimate;
    }

    public void setStepDela(long stepDela) {
        _StepDela = stepDela;
    }

    public void setDuration(long duration) {
        _Duration = duration;
    }


    protected abstract List<Animator> getAnimators(View view, int position, boolean isSelected);

}
