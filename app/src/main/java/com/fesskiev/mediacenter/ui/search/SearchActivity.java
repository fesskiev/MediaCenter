package com.fesskiev.mediacenter.ui.search;


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

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.players.AudioPlayer;
import com.fesskiev.mediacenter.data.model.MediaFile;
import com.fesskiev.mediacenter.ui.audio.player.AudioPlayerActivity;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity {

    private Disposable subscription;
    private SearchView searchView;
    private SearchAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViewById(R.id.backIcon).setOnClickListener(v -> onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.recycleView);
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
        searchView = findViewById(R.id.searchView);
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
        DataRepository repository = MediaApplication.getInstance().getRepository();
        RxUtils.unsubscribe(subscription);
        subscription = repository
                .getSearchAudioFiles(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> adapter.refreshAdapter(audioFiles, search));
    }


    private void setupTransitions() {
        getWindow().getEnterTransition()
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
                searchTitle = v.findViewById(R.id.itemSearch);
            }
        }

        public class ResultViewHolder extends RecyclerView.ViewHolder {

            TextView duration;
            TextView title;
            TextView filePath;
            ImageView cover;

            public ResultViewHolder(View v) {
                super(v);
                v.setOnClickListener(v1 -> startPlayerActivity(getAdapterPosition()));
                duration = v.findViewById(R.id.itemDuration);
                title = v.findViewById(R.id.itemTitle);
                filePath = v.findViewById(R.id.itemPath);
                cover = v.findViewById(R.id.itemCover);
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

        private void startPlayerActivity(int position) {
            MediaFile mediaFile = mediaFiles.get(position);
            if (mediaFile != null) {
                switch (mediaFile.getMediaType()) {
                    case VIDEO:
                        break;
                    case AUDIO:
                        Activity act = activity.get();
                        if (act != null) {
                            AudioPlayer audioPlayer = MediaApplication.getInstance().getAudioPlayer();

                            audioPlayer.setCurrentAudioFileAndPlay((AudioFile) mediaFile);
                            AudioPlayerActivity.startPlayerActivity(act);
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
