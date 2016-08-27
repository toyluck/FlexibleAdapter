package com.example.anybody.rewriteflexibleadapter;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.anybody.rewriteflexibleadapter.adapter.FlexibleAnimatorAdapter;
import com.example.anybody.rewriteflexibleadapter.adapter.FlexibleViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by anybody on 2016/8/26.
 */
public class ExampleAdapter extends FlexibleAnimatorAdapter<FlexibleViewHolder, Item> {

    private static final String TAG = ExampleAdapter.class.getSimpleName();
    private static final int EXAMPLE_VIEW_TYPE = 0;
    private static final int ROW_RECY_VIEW = 1;
    public final FlexibleViewHolder.OnListItemClickListener _listener;
    private final Context _context;

    public ExampleAdapter(Object listener, RecyclerView recyclerView) {
        super(DataService.getInstance().getItemsWithTag(TAG), listener, recyclerView);
        _listener = (FlexibleViewHolder.OnListItemClickListener) listener;
        _context = recyclerView.getContext();
        userLearnedSelection();
        if (!isEmpty()) addUserLearndSelection();
    }

    private void addUserLearndSelection() {
        if (!DataService.userLearnedSelection && !hasSearchText()) {
            //Define Example View
            Item item = new Item();
            item.setId(0);
            item.setTitle(_context.getString(R.string.uls_title));
            item.setSubTitle(_context.getString(R.string.uls_subtitle));
            _items.add(0, item);
        }

    }

    @Override
    public int getItemViewType(int position) {

        return position == 0 && DataService.userLearnedSelection && !hasSearchText() ? EXAMPLE_VIEW_TYPE : ROW_RECY_VIEW;
    }

    @Override
    public boolean isEmpty() {
        return !DataService.userLearnedSelection && _items.size() == 1 || super.isEmpty();
    }

    @Override
    protected List<Animator> getAnimators(View view, int position, boolean isSelected) {
        List<Animator> animators = new ArrayList<>();
        addAlphaAnimator(animators, view, 0f);

        switch (getItemViewType(position)) {
            case EXAMPLE_VIEW_TYPE:
                addScaleInAnimator(animators, view, 0.0f);
                break;
            default:
                if (isSelected) {
                    addSlideInFromLeftAnimator(animators, view, 0.5f);
                } else {
                    addSlideInFromRightAnimator(animators, view, 0.5f);
                }
                break;
        }
        return animators;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View inflatedView;
        switch (viewType) {
            case EXAMPLE_VIEW_TYPE:
                inflatedView = layoutInflater.inflate(R.layout.recy_example, parent, false);
                return new ExampleViewHolder(inflatedView, this);
            default:
                inflatedView = layoutInflater.inflate(R.layout.recy_row, parent, false);
                return new ViewHOlder(inflatedView, this);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Item item = _items.get(position);
        switch (getItemViewType(position)) {
            case EXAMPLE_VIEW_TYPE:
                ExampleViewHolder exampleViewHolder = (ExampleViewHolder) holder;
                exampleViewHolder.mImageView.setImageResource(R.drawable.ic_account_circle_white_24dp);
                exampleViewHolder.itemView.setActivated(true);
                exampleViewHolder.mTitle.setSelected(true);//For marquee
                exampleViewHolder.mTitle.setText(Html.fromHtml(item.getTitle()));
                exampleViewHolder.mSubtitle.setText(Html.fromHtml(item.getSubTitle()));
                animateView(holder.itemView, position, false);
                break;
            default:
                ViewHOlder viewHOlder = (ViewHOlder) holder;
                viewHOlder.mImageView.setActivated(isSelected(position));

                if (isSelected(position)) {
                    viewHOlder.mImageView.setBackgroundDrawable(((ViewHOlder) holder).mImageView.getResources().getDrawable(R.drawable.image_round_selected));
                    animateView(viewHOlder.itemView, position, true);
                } else {
                    viewHOlder.mImageView.setBackgroundDrawable(((ViewHOlder) holder).mImageView.getResources().getDrawable(R.drawable.image_round_normal));

                    animateView(viewHOlder.itemView, position, false);
                    if (hasSearchText()) {
                        setHighlightText(viewHOlder.mTitle, item.getTitle(), getSearchText());
                        setHighlightText(viewHOlder.mSubtitle, item.getTitle(), getSearchText());
                    } else {
                        viewHOlder.mTitle.setText(item.getTitle());
                        viewHOlder.mSubtitle.setText(item.getSubTitle());
                    }
                }
                break;
        }

    }

    private void setHighlightText(TextView tv, String targetText, String searchText) {
        Spannable spannable = Spannable.Factory.getInstance().newSpannable(targetText);
        String text = targetText.trim().toLowerCase(Locale.getDefault());
        int i = text.indexOf(searchText);
        if (i != -1) {
            spannable.setSpan(new ForegroundColorSpan(Utils.getColorAccent(tv.getContext())), i, i + searchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), i, i + searchText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv.setText(spannable, TextView.BufferType.SPANNABLE);
        } else {
            tv.setText(targetText, TextView.BufferType.NORMAL);
        }
    }

    @Override
    public void updateDataSet(String param) {
        _items = DataService.getInstance().getItemsWithTag(param);
        if (!super.isEmpty()) addUserLearndSelection();
        filterItems(_items);
        notifyDataSetChanged();
        updateEmptyView(_items.size());
    }

    static class ExampleViewHolder extends FlexibleViewHolder {
        ImageView mImageView;
        TextView mTitle;
        TextView mSubtitle;
        ImageView mDismissIcon;

        ExampleViewHolder(View view, final ExampleAdapter adapter) {
            super(view, adapter, null);
            mTitle = (TextView) view.findViewById(R.id.title);
            mSubtitle = (TextView) view.findViewById(R.id.subtitle);
            mImageView = (ImageView) view.findViewById(R.id.image);
            mDismissIcon = (ImageView) view.findViewById(R.id.dismiss_icon);
            mDismissIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.userLearnedSelection();
                }
            });
        }


    }

    private void userLearnedSelection() {
        DataService.userLearnedSelection = true;
        _items.remove(0);
        notifyItemRemoved(0);
    }

    private static class ViewHOlder extends FlexibleViewHolder {
        ImageView mImageView;
        TextView mTitle;
        TextView mSubtitle;

        ViewHOlder(View view, final ExampleAdapter adapter) {
            super(view, adapter, adapter._listener);

            this.mTitle = (TextView) view.findViewById(R.id.title);
            this.mSubtitle = (TextView) view.findViewById(R.id.subtitle);
            this.mImageView = (ImageView) view.findViewById(R.id.image);
            this.mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _clickListener.onListItemLongClick(getAdapterPosition());
                    toggleActivation();
                }
            });

        }

        @Override
        protected void toggleActivation() {
            super.toggleActivation();
            mImageView.setBackgroundDrawable(this.itemView.isActivated() ?
                    mImageView.getResources().getDrawable(R.drawable.image_round_selected) :
                    mImageView.getResources().getDrawable(R.drawable.image_round_normal));
        }
    }


}
