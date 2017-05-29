package com.fesskiev.mediacenter.ui.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.data.model.search.Album;
import com.fesskiev.mediacenter.data.model.search.Image;
import com.fesskiev.mediacenter.data.source.remote.ErrorHelper;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;

import java.util.List;

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

    private AudioFolder audioFolder;
    private Subscription subscription;

    private MaterialProgressBar progressBar;
    private String artist;
    private String album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_album);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
        findViewById(R.id.searchAlbumFab).setOnClickListener(v -> loadAlbum());

        if (savedInstanceState != null) {
            audioFolder = savedInstanceState.getParcelable(EXTRA_AUDIO_FOLDER);
        } else {
            audioFolder = getIntent().getExtras().getParcelable(EXTRA_AUDIO_FOLDER);
        }

    }

    private void loadAlbum() {
        String[] parts = audioFolder.folderName.split("-");
        if (parts.length == 2) {
            showProgressBar();
            subscription = MediaApplication.getInstance()
                    .getRepository().getAlbum(parts[0].trim().trim(), parts[1].trim().trim())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(data -> parseAlbum(data.getAlbum()), this::handleError);
        } else {
            showEnterCorrectArtistAlbum();
        }
    }

    private void parseAlbum(Album album) {
        hideProgressBar();
        String artist = album.getArtist();
        String name = album.getName();

        List<Image> images = album.getImage();
        for (Image image : images) {
            AppLog.DEBUG("image: " + image.toString());
        }
    }

    private void handleError(Throwable throwable) {
        hideProgressBar();
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

    private void showEnterCorrectArtistAlbum() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unsubscribe();
    }

    public void unsubscribe() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

}
