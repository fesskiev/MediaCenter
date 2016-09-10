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
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaDataSource;
import com.fesskiev.player.model.AudioFile;
import com.fesskiev.player.model.AudioPlayer;
import com.fesskiev.player.model.MediaFile;
import com.fesskiev.player.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.player.utils.BitmapHelper;
import com.fesskiev.player.utils.RxUtils;
import com.fesskiev.player.utils.Utils;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.lang.ref.WeakReference;
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
                querySearch(query, false);
                Utils.hideKeyboard(SearchActivity.this);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                querySearch(query, true);
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {

        });
    }

    private void querySearch(String query, boolean search) {
        MediaDataSource dataSource = MediaApplication.getInstance().getMediaDataSource();
        RxUtils.unsubscribe(subscription);
        subscription = dataSource
                .getSearchAudioFiles(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> {
                    adapter.refreshAdapter(audioFiles, search);
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

    private static class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_SEARCH = 0;
        private static final int VIEW_TYPE_RESULT = 1;

        private WeakReference<Activity> activity;
        private List<MediaFile> mediaFiles;
        private boolean search;


        public SearchAdapter(Activity activity) {
            this.activity = new WeakReference<>(activity);
            this.mediaFiles = new ArrayList<>();
        }

        public class SearchViewHolder extends RecyclerView.ViewHolder {

            TextView searchTitle;

            public SearchViewHolder(View v) {
                super(v);
                searchTitle = (TextView) v.findViewById(R.id.itemSearch);
            }
        }

        public class ResultViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView title;
            TextView filePath;
            ImageView cover;

            public ResultViewHolder(View v) {
                super(v);
                v.setOnClickListener(v1 -> startPlayerActivity(getAdapterPosition(), cover));
                duration = (TextView) v.findViewById(R.id.itemDuration);
                title = (TextView) v.findViewById(R.id.itemTitle);
                filePath = (TextView) v.findViewById(R.id.filePath);
                cover = (ImageView) v.findViewById(R.id.itemCover);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            switch (viewType) {
                case VIEW_TYPE_SEARCH:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_search, parent, false);
                    return new SearchViewHolder(v);
                case VIEW_TYPE_RESULT:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_audio_track, parent, false);
                    return new ResultViewHolder(v);

            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_SEARCH:
                    createSearchItem((SearchViewHolder) holder, position);
                    break;
                case VIEW_TYPE_RESULT:
                    createResultItem((ResultViewHolder) holder, position);
                    break;
            }
        }

        private void startPlayerActivity(int position, View cover) {
            MediaFile mediaFile = mediaFiles.get(position);
            if (mediaFile != null) {
                switch (mediaFile.getMediaType()) {
                    case VIDEO:
                        break;
                    case AUDIO:
                        Activity act = activity.get();
                        if (act != null) {
                            AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();
                            audioPlayer.setCurrentAudioFile((AudioFile) mediaFile);
                            audioPlayer.position = position;
                            AudioPlayerActivity.startPlayerActivity(act, true, cover);
                        }
                        break;
                }
            }
        }

        private void createResultItem(ResultViewHolder holder, int position) {
            MediaFile mediaFile = mediaFiles.get(position);
            if (mediaFile != null) {

                BitmapHelper.getInstance().loadTrackListArtwork(mediaFile, holder.cover);

                holder.duration.setText(Utils.getDurationString(mediaFile.getLength()));
                holder.title.setText(mediaFile.getTitle());
                holder.filePath.setText(mediaFile.getFilePath());
            }
        }

        private void createSearchItem(SearchViewHolder holder, int position) {
            MediaFile mediaFile = mediaFiles.get(position);
            if (mediaFile != null) {
                holder.searchTitle.setText(mediaFile.getTitle());
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (search) {
                return VIEW_TYPE_SEARCH;
            }
            return VIEW_TYPE_RESULT;
        }

        @Override
        public int getItemCount() {
            return mediaFiles.size();
        }

        public void refreshAdapter(List<AudioFile> queryResult, boolean search) {
            this.search = search;
            mediaFiles.clear();
            mediaFiles.addAll(queryResult);
            notifyDataSetChanged();
        }
    }
}
