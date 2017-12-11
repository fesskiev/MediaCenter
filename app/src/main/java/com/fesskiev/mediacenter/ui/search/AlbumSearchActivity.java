package com.fesskiev.mediacenter.ui.search;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.search.Album;
import com.fesskiev.mediacenter.data.model.search.Tag;
import com.fesskiev.mediacenter.data.model.search.Tags;
import com.fesskiev.mediacenter.data.model.search.Tracks;
import com.fesskiev.mediacenter.ui.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.search.Image;
import com.fesskiev.mediacenter.data.model.search.Track;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.progress.MaterialProgressBar;
import com.fesskiev.mediacenter.widgets.dialogs.SelectImageDialog;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class AlbumSearchActivity extends AnalyticsActivity {

    public static void startSearchDataActivity(Activity activity, AudioFolder audioFolder) {
        Intent intent = new Intent(activity, AlbumSearchActivity.class);
        intent.putExtra(EXTRA_AUDIO_FOLDER, audioFolder);
        activity.startActivity(intent);
    }

    private final static String EXTRA_AUDIO_FOLDER = "com.fesskiev.mediacenter.EXTRA_AUDIO_FOLDER";
    private final static String EXTRA_ARTIST = "com.fesskiev.mediacenter.EXTRA_ARTIST";
    private final static String EXTRA_ALBUM = "com.fesskiev.mediacenter.EXTRA_ALBUM";

    private AudioFolder audioFolder;

    private SearchAdapter adapter;

    private MaterialProgressBar progressBar;
    private TextInputLayout artistInputLayout;
    private TextInputLayout albumInputLayout;
    private EditText artistEditText;
    private EditText albumEditText;
    private ImageView albumCover;
    private TextView artistResult;
    private TextView albumResult;
    private TextView artistURL;
    private TextView tagsResult;
    private View albumRoot;

    private List<Image> images;
    private String artist;
    private String album;

    private AlbumSearchViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_album);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new SearchAdapter();
        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.searchAlbumFab).setOnClickListener(v -> loadAlbum());

        albumRoot = findViewById(R.id.albumViewRoot);

        albumCover = findViewById(R.id.albumCover);
        albumCover.setOnClickListener(v -> openChooseImageQualityDialog());

        artistEditText = findViewById(R.id.editArtist);
        albumEditText = findViewById(R.id.editAlbum);

        artistResult = findViewById(R.id.artistNameResult);
        albumResult = findViewById(R.id.albumNameResult);
        tagsResult = findViewById(R.id.tagsResult);
        artistURL = findViewById(R.id.artistUrl);
        artistURL.setOnClickListener(v -> openUrl(artistURL.getText().toString()));

        artistInputLayout = findViewById(R.id.artistTextInputLayout);
        albumInputLayout = findViewById(R.id.albumTextInputLayout);

        if (savedInstanceState != null) {
            audioFolder = savedInstanceState.getParcelable(EXTRA_AUDIO_FOLDER);
            artist = savedInstanceState.getString(EXTRA_ARTIST);
            album = savedInstanceState.getString(EXTRA_ALBUM);
        } else {
            audioFolder = getIntent().getExtras().getParcelable(EXTRA_AUDIO_FOLDER);
        }

        AlbumTextWatcher albumTextWatcher = new AlbumTextWatcher();

        artistEditText.addTextChangedListener(albumTextWatcher);
        albumEditText.addTextChangedListener(albumTextWatcher);

        observeData();
    }

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(AlbumSearchViewModel.class);
        viewModel.getEnterAlbumErrorLiveData().observe(this, Void -> {
            showEnterAlbumError();
            hideProgressBar();
        });
        viewModel.getEnterArtistErrorLiveData().observe(this, Void -> {
            showEnterArtistError();
            hideProgressBar();
        });
        viewModel.getResponseAlbumNotFoundLiveData().observe(this, Void -> {
            shoAlbumNotFoundError();
            hideProgressBar();
        });
        viewModel.getResponseErrorLiveData().observe(this, message -> {
            createResponseErrorSnackBar(message);
            hideProgressBar();
        });
        viewModel.getAlbumCoverLiveData().observe(this, this::setAlbumCover);
        viewModel.getSuccessSaveBitmapLiveData().observe(this, Void -> {
            showSuccessSaveBitmap();
            hideProgressBar();
        });
        viewModel.getErrorSaveBitmapLiveData().observe(this, Void -> {
            showErrorSaveBitmap();
            hideProgressBar();
        });
        viewModel.getAlbumLiveData().observe(this, album -> {
            setAlbum(album);
            hideProgressBar();
        });
    }

    private void loadAlbum() {
        Utils.hideKeyboard(this);
        hideEnterArtistError();
        hideEnterAlbumError();
        showProgressBar();
        viewModel.loadAlbum(artist, album);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (artist == null && album == null) {
            parseAudioFolderName(audioFolder);
        }
        setArtistAndAlbum();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_AUDIO_FOLDER, audioFolder);
    }

    @Override
    public String getActivityName() {
        return this.getLocalClassName();
    }

    public void parseAudioFolderName(AudioFolder audioFolder) {
        String[] parts = audioFolder.folderName.split("-");
        if (parts.length >= 2) {
            artist = parts[0].trim();
            album = parts[1].trim();
        }
    }

    private void setAlbum(Album album) {
        String artist = album.getArtist();
        String albumName = album.getName();
        String url = album.getUrl();
        images = album.getImage();

        if (artist != null) {
            artistResult.setText(artist);
        }
        if (albumName != null) {
            albumResult.setText(albumName);
        }
        if (url != null) {
            artistURL.setText(url);
        }
        Tags tags = album.getTags();
        if (tags != null) {
            List<Tag> tag = tags.getTag();
            if (tag != null) {
                StringBuilder sb = new StringBuilder();
                for (Tag t : tag) {
                    sb.append(t.getName());
                    sb.append(" ");
                }
                tagsResult.setText(sb.toString());
            }
        }
        albumRoot.setVisibility(View.VISIBLE);

        Tracks tracks = album.getTracks();
        if (tracks != null) {
            List<Track> listTracks = tracks.getTrack();
            if (listTracks != null && !listTracks.isEmpty()) {
                adapter.refreshAdapter(listTracks);
            }
        }
        if (hasImage(album)) {
            enableCoverClick();
            showLoadSuccess();
        } else {
            disableCoverClick();
        }
    }

    private boolean hasImage(Album album) {
        List<Image> images = album.getImage();
        if (images == null) {
            return false;
        }
        for (Image image : images) {
            String text = image.getText();
            if (text != null && !TextUtils.isEmpty(text)) {
                return true;
            }
        }
        return false;
    }

    private void openChooseImageQualityDialog() {
        if (images != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            SelectImageDialog dialog = SelectImageDialog.newInstance((ArrayList<Image>) images);
            dialog.show(transaction, SelectImageDialog.class.getName());
            dialog.setOnSelectedImageListener(image -> viewModel.loadSelectedImage(image, audioFolder));
        }
    }

    private void setAlbumCover(Bitmap bitmap) {
        albumCover.setImageBitmap(bitmap);
    }

    private void openUrl(String url) {
        Utils.openBrowserURL(this, url);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showEnterAlbumError() {
        albumInputLayout.setErrorEnabled(true);
        albumInputLayout.setError(getString(R.string.search_album_enter_error));
    }

    private void setArtistAndAlbum() {
        artistEditText.setText(artist);
        albumEditText.setText(album);
    }

    private void disableCoverClick() {
        albumCover.setEnabled(false);
        albumCover.setClickable(false);
    }

    private void enableCoverClick() {
        albumCover.setEnabled(true);
        albumCover.setClickable(true);
    }

    private void showEnterArtistError() {
        artistInputLayout.setErrorEnabled(true);
        artistInputLayout.setError(getString(R.string.search_artist_enter_error));
    }

    private void hideEnterAlbumError() {
        albumInputLayout.setError(null);
        albumInputLayout.setErrorEnabled(false);
    }

    private void hideEnterArtistError() {
        artistInputLayout.setError(null);
        artistInputLayout.setErrorEnabled(false);

    }

    private void showErrorSaveBitmap() {
        Utils.showCustomSnackbar(findViewById(R.id.albumSearchRoot),
                getApplicationContext(), getString(R.string.search_save_bitmap_error), Snackbar.LENGTH_LONG).show();
    }

    private void showSuccessSaveBitmap() {
        Utils.showCustomSnackbar(findViewById(R.id.albumSearchRoot),
                getApplicationContext(), getString(R.string.search_save_bitmap_success), Snackbar.LENGTH_LONG).show();
    }

    private void showLoadSuccess() {
        Utils.showCustomSnackbar(findViewById(R.id.albumSearchRoot),
                getApplicationContext(), getString(R.string.search_load_album_success),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.button_ok), view -> openChooseImageQualityDialog()).show();
    }

    private void shoAlbumNotFoundError() {
        Utils.showCustomSnackbar(findViewById(R.id.albumSearchRoot),
                getApplicationContext(), getString(R.string.search_load_album_not_found), Snackbar.LENGTH_LONG).show();
    }

    private void createResponseErrorSnackBar(String message) {
        Utils.showInternetErrorCustomSnackbar(findViewById(R.id.albumSearchRoot), getApplicationContext(), message,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.snackbar_error_try_again, v -> loadAlbum())
                .show();
    }

    private class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

        private List<Track> tracks;

        public SearchAdapter() {
            tracks = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView trackName;
            TextView trackUrl;
            TextView trackDuration;

            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(v1 -> openTrackUrl(getAdapterPosition()));

                trackName = v.findViewById(R.id.itemTrackName);
                trackUrl = v.findViewById(R.id.itemUrl);
                trackDuration = v.findViewById(R.id.itemDuration);
            }

            private void openTrackUrl(int position) {
                Track track = tracks.get(position);
                if (track != null) {
                    openUrl(track.getUrl());
                }
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_search_album_track, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Track track = tracks.get(position);
            if (track != null) {
                holder.trackName.setText(track.getName());
                holder.trackUrl.setText(track.getUrl());
                holder.trackDuration.setText(Utils.getTimeFromSecondsString(track.getDuration()));
            }
        }

        @Override
        public int getItemCount() {
            return tracks.size();
        }

        public void refreshAdapter(List<Track> tracks) {
            this.tracks.clear();
            this.tracks.addAll(tracks);
            notifyDataSetChanged();
        }

    }

    private class AlbumTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable == artistEditText.getEditableText()) {
                artist = editable.toString();
            } else if (editable == albumEditText.getEditableText()) {
                album = editable.toString();
            }
        }
    }
}
