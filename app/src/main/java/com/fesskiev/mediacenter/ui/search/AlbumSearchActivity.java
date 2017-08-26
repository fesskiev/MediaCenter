package com.fesskiev.mediacenter.ui.search;

import android.app.Activity;
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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.search.Album;
import com.fesskiev.mediacenter.data.model.search.Image;
import com.fesskiev.mediacenter.data.model.search.Tag;
import com.fesskiev.mediacenter.data.model.search.Tags;
import com.fesskiev.mediacenter.data.model.search.Track;
import com.fesskiev.mediacenter.data.model.search.Tracks;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.source.remote.ErrorHelper;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;
import com.fesskiev.mediacenter.widgets.dialogs.SelectImageDialog;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class AlbumSearchActivity extends AnalyticsActivity {

    public static void startSearchDataActivity(Activity activity, AudioFolder audioFolder) {
        Intent intent = new Intent(activity, AlbumSearchActivity.class);
        intent.putExtra(EXTRA_AUDIO_FOLDER, audioFolder);
        activity.startActivity(intent);
    }

    private final static String EXTRA_AUDIO_FOLDER = "com.fesskiev.mediacenter.EXTRA_AUDIO_FOLDER";


    private DataRepository repository;
    private Subscription subscription;
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

    private String artist;
    private String album;

    private List<Image> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_album);

        repository = MediaApplication.getInstance().getRepository();

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
        } else {
            audioFolder = getIntent().getExtras().getParcelable(EXTRA_AUDIO_FOLDER);
        }

        AlbumTextWatcher albumTextWatcher = new AlbumTextWatcher();

        artistEditText.addTextChangedListener(albumTextWatcher);
        albumEditText.addTextChangedListener(albumTextWatcher);

        parseAudioFolderName();
    }


    @Override
    protected void onPause() {
        super.onPause();
        unsubscribe();
    }

    public void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
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


    private void parseAudioFolderName() {
        String[] parts = audioFolder.folderName.split("-");
        if (parts.length >= 2) {
            artist = parts[0].trim();
            album = parts[1].trim();
            artistEditText.setText(artist);
            albumEditText.setText(album);
        }
    }


    private void loadAlbum() {
        if (artist == null || artist.isEmpty()) {
            showEnterArtistError();
            return;
        }
        if (album == null || album.isEmpty()) {
            showEnterAlbumError();
            return;
        }
        Utils.hideKeyboard(this);
        showProgressBar();
        subscription = repository.getAlbum(artist.trim(), album.trim())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(albumResponse -> parseAlbum(albumResponse.getAlbum()))
                .observeOn(Schedulers.io())
                .flatMap(albumResponse -> parseAlbumCover(albumResponse.getAlbum()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setLoadedCover, this::handleError);
    }

    private void setLoadedCover(Bitmap bitmap) {
        if (bitmap != null) {
            albumCover.setImageBitmap(bitmap);
        }
    }

    private Observable<Bitmap> parseAlbumCover(Album album) {
        if (album != null) {
            images = album.getImage();
            if (images != null) {
                for (Image image : images) {
                    AppLog.DEBUG("image: " + image.toString());
                    if (image.getSize().equals("large")) {
                        return BitmapHelper.getInstance().getBitmapFromURL(image.getText());
                    }
                }
            }
        }
        return Observable.empty();
    }

    private void parseAlbum(Album album) {
        if (album == null) {
            showLoadAlbumError();
            hideProgressBar();
            return;
        }
        String artist = album.getArtist();
        String albumName = album.getName();
        String url = album.getUrl();

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
        hideProgressBar();
        showLoadSuccess();
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

    private void openChooseImageQualityDialog() {
        if (images != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.addToBackStack(null);
            SelectImageDialog dialog = SelectImageDialog.newInstance((ArrayList<Image>) images);
            dialog.show(transaction, SelectImageDialog.class.getName());
            dialog.setOnSelectedImageListener(this::loadSelectedImage);
        }
    }

    public void loadSelectedImage(Image image) {
        subscription = BitmapHelper.getInstance().getBitmapFromURL(image.getText())
                .subscribeOn(Schedulers.io())
                .doOnNext(bitmap -> removeFolderImages())
                .doOnNext(this::saveArtworkAndUpdateFolder)
                .flatMap(bitmap -> repository.getAudioTracks(audioFolder.id))
                .doOnNext(audioFiles -> {
                    for (AudioFile audioFile : audioFiles) {
                        audioFile.folderArtworkPath = audioFolder.folderImage.getAbsolutePath();
                        repository.updateAudioFile(audioFile);
                    }
                })
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioFiles -> showSuccessSaveBitmap(),
                        throwable -> showErrorSaveBitmap());
    }


    private void saveArtworkAndUpdateFolder(Bitmap bitmap) {
        try {
            File path = File.createTempFile(audioFolder.folderName, ".jpg", audioFolder.folderPath);

            BitmapHelper.getInstance().saveBitmap(bitmap, path);

            audioFolder.folderImage = path;

            repository.updateAudioFolder(audioFolder);
            repository.getMemorySource().setCacheFoldersDirty(true);

        } catch (IOException e) {
            e.printStackTrace();
            showErrorSaveBitmap();
        }
    }

    private void removeFolderImages() {
        File[] filterImages = audioFolder.folderPath.listFiles(FileSystemService.folderImageFilter());
        if (filterImages != null && filterImages.length > 0) {
            for (File image : filterImages) {
                image.delete();
            }
        }
    }

    private void handleError(Throwable throwable) {
        hideProgressBar();
        hideEnterArtistError();
        hideEnterAlbumError();
        ErrorHelper.getInstance().createErrorSnackBar(getCurrentFocus(), throwable,
                new ErrorHelper.OnErrorHandlerListener() {
                    @Override
                    public void tryRequestAgain() {
                        loadAlbum();
                    }

                    @Override
                    public void show(Snackbar snackbar) {

                    }

                    @Override
                    public void hide(Snackbar snackbar) {

                    }
                });
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

    private void hideEnterAlbumError() {
        albumInputLayout.setError(null);
        albumInputLayout.setErrorEnabled(false);
    }

    private void showEnterArtistError() {
        artistInputLayout.setErrorEnabled(true);
        artistInputLayout.setError(getString(R.string.search_artist_enter_error));
    }

    private void hideEnterArtistError() {
        artistInputLayout.setError(null);
        artistInputLayout.setErrorEnabled(false);

    }

    private void showLoadAlbumError() {
        Utils.showCustomSnackbar(findViewById(R.id.albumSearchRoot),
                getApplicationContext(), getString(R.string.search_load_album_error), Snackbar.LENGTH_LONG).show();
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

}
