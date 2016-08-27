package com.example.anybody.rewriteflexibleadapter;

import android.app.SearchManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.anybody.rewriteflexibleadapter.adapter.FlexibleAdapter;
import com.example.anybody.rewriteflexibleadapter.adapter.FlexibleViewHolder;
import com.example.anybody.rewriteflexibleadapter.adapter.SelectableAdapter;
import com.example.anybody.rewriteflexibleadapter.fastscroller.FastScroller;

public class MainActivity extends AppCompatActivity implements FlexibleViewHolder.OnListItemClickListener, SearchView.OnQueryTextListener, ActionMode.Callback, FlexibleAdapter.OnDeleteCompeleteListener {

    private SwipeRefreshLayout _swipeRefreshLayout;
    private RecyclerView _recyclerView;
    private FastScroller _fastScroller;
    private FloatingActionButton _floatingActionButton;
    private TextView _emptyView;
    private ExampleAdapter _exampleAdapter;
    private final Handler _SwipeHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 0: //Stop
                    _swipeRefreshLayout.setRefreshing(false);
                    _swipeRefreshLayout.setEnabled(true);
                    return true;
                case 1: //1 Start
                    _swipeRefreshLayout.setRefreshing(true);
                    _swipeRefreshLayout.setEnabled(false);
                    return true;
                default:
                    return false;
            }
        }
    });
    private ActionMode _actionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        _recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        _fastScroller = (FastScroller) findViewById(R.id.fast_scroller);
        _floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        _emptyView = (TextView) findViewById(R.id.empty);

    }

    @Override
    protected void onStart() {
        super.onStart();
        initRecyclerView();
        initSwiptLayout();
    }

    private void initRecyclerView() {
        _exampleAdapter = new ExampleAdapter(this, _recyclerView);
        _exampleAdapter.enablelog(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        _recyclerView.setLayoutManager(layoutManager);
        _recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
        _recyclerView.setAdapter(_exampleAdapter);

    }

    private void initSwiptLayout() {
        _swipeRefreshLayout.setDistanceToTriggerSync(300);
        _swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple, android.R.color.holo_blue_dark,
                android.R.color.holo_green_light, android.R.color.holo_red_light);

        _swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                _exampleAdapter.updateDataSet();
                _swipeRefreshLayout.setEnabled(false);
                _SwipeHandler.sendEmptyMessageDelayed(0, 2000L);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        initSearchView(item);
        return super.onCreateOptionsMenu(menu);
    }

    private void initSearchView(MenuItem item) {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
        searchView.setQueryHint(getString(R.string.action_query));

        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        if (_exampleAdapter.hasSearchText()) {
            searchView.setQuery(_exampleAdapter.getSearchText(), false);
            searchView.setIconified(false);
        } else {
            searchView.setIconified(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onListItemClick(int position) {
        if (_actionMode != null) {
            toggleSelection(position);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onListItemLongClick(int position) {
        //打开ActionModel
        if (_actionMode == null)
            _actionMode = startSupportActionMode(this);
        toggleSelection(position);
        return true;
    }

    private void toggleSelection(int position) {
        _exampleAdapter.togglePosition(position, false);
        int count = _exampleAdapter.getSelectedCount();
        if (count == 0) {
            _actionMode.finish();
        } else {
            setActionTitle(count);
        }

    }

    private void setActionTitle(int count) {
        _actionMode.setTitle(String.valueOf(count) + "" +
                (count == 1 ? getString(R.string.action_selection_one) :
                        getString(R.string.action_selection_many)));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQueryTextChange(query);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!_exampleAdapter.hasSearchText() || !TextUtils.equals(_exampleAdapter.getSearchText(), newText)) {
            _exampleAdapter.setSearchText(newText);
            _exampleAdapter.updateDataSet();
            if (_exampleAdapter.hasSearchText()) {
                //hiding fab
                ViewCompat.animate(_floatingActionButton).alpha(0).setDuration(300)
                        .scaleX(0).scaleY(0).start();
            } else {
                //show fab
                ViewCompat.animate(_floatingActionButton).alpha(1).setDuration(300)
                        .scaleX(1).scaleY(1).start();

            }
        }
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        int menuId = R.menu.menu_action_list;
        mode.getMenuInflater().inflate(menuId, menu);
        _exampleAdapter.setModel(SelectableAdapter.MODELS.MODE_MULTI);

        if (Utils.hasMarshmallow()) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccentDark_light, getTheme()));
        } else {
            getWindow().setStatusBarColor(getColor(R.color.colorAccentDark_light));

        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_selectAll:

                _exampleAdapter.selectAll();

                break;
            case R.id.action_delete:
                _exampleAdapter.removeItems(_exampleAdapter.getSelectedItems());

                Snackbar snackbar = Snackbar.make(_recyclerView, getString(R.string.action_snack), Snackbar.LENGTH_SHORT);
                snackbar.setAction("撤销", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        _exampleAdapter.restoreDeleteItems();
                        _SwipeHandler.sendEmptyMessage(0);
                        _exampleAdapter.stopUndoTimer();
                    }
                });
                snackbar.show();
               _SwipeHandler.sendEmptyMessage(1);
                _exampleAdapter.startUndoTimer(7000, this);
                _actionMode.finish();
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        _exampleAdapter.setModel(SelectableAdapter.MODELS.MODE_SINGLE);
        _exampleAdapter.clearSelections();
        _actionMode = null;
        if (Utils.hasMarshmallow()) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark_light, getTheme()));
        } else {
            getWindow().setStatusBarColor(getColor(R.color.colorPrimaryDark_light));
        }
    }

    @Override
    public void confirmDeleted() {
        for (Item item : _exampleAdapter.getDeletedItems()) {
            DataService.getInstance().removeItem(item);
        }
    }
}
