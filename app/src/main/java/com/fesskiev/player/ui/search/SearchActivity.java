package com.fesskiev.player.ui.search;


import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaDataSource;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity {

    private Subscription subscription;
    private SearchView searchView;
    private SearchAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViewById(R.id.backIcon).setOnClickListener(v -> onBackPressed());

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));

        adapter = new SearchAdapter(this);
        recyclerView.setAdapter(adapter);

        setupSearchView();
        setupTransitions();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                searchView.setQuery(query, false);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        RxUtils.unsubscribe(subscription);
    }


    private void setupSearchView() {
        searchView = (SearchView) findViewById(R.id.searchView);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                querySearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                querySearch(query);
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {

        });
    }

    private void querySearch(String query) {
        MediaDataSource dataSource = MediaApplication.getInstance().getMediaDataSource();
        RxUtils.unsubscribe(subscription);
        subscription = dataSource
                .getSearchAudioFiles(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> {
                    adapter.refreshAdapter(audioFiles);
                }, throwable -> {

                });
    }


    private void setupTransitions() {
        getWindow()
                .getEnterTransition()
                .addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(Transition transition) {

                    }

                    @Override
                    public void onTransitionEnd(Transition transition) {
                        searchView.requestFocus();
                    }

                    @Override
                    public void onTransitionCancel(Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(Transition transition) {

                    }
                });
    }

    private static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

        private Activity activity;
        private List<AudioFile> audioFiles;


        public SearchAdapter(Activity activity) {
            this.activity = activity;
            this.audioFiles = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView searchTitle;

            public ViewHolder(View v) {
                super(v);
                searchTitle = (TextView) v.findViewById(R.id.itemSearch);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AudioFile audioFile = audioFiles.get(position);
            if(audioFile != null){
                holder.searchTitle.setText(audioFile.title);
            }
        }

        @Override
        public int getItemCount() {
            return audioFiles.size();
        }

        public void refreshAdapter(List<AudioFile> queryResult) {
            audioFiles.clear();
            audioFiles.addAll(queryResult);
            notifyDataSetChanged();
        }
    }
}
