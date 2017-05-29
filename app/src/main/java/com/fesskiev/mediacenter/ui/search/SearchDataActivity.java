package com.fesskiev.mediacenter.ui.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.analytics.AnalyticsActivity;
import com.fesskiev.mediacenter.data.model.AudioFolder;
import com.fesskiev.mediacenter.utils.AppLog;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SearchDataActivity extends AnalyticsActivity {

    public static void startSearchDataActivity(Activity activity, AudioFolder audioFolder) {
        Intent intent = new Intent(activity, SearchDataActivity.class);
        intent.putExtra(EXTRA_AUDIO_FOLDER, audioFolder);
        activity.startActivity(intent);
    }

    private final static String EXTRA_AUDIO_FOLDER = "com.fesskiev.mediacenter.EXTRA_AUDIO_FOLDER";

    private AudioFolder audioFolder;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState != null) {
            audioFolder = savedInstanceState.getParcelable(EXTRA_AUDIO_FOLDER);
        } else {
            audioFolder = getIntent().getExtras().getParcelable(EXTRA_AUDIO_FOLDER);
        }

        loadAlbum();
    }

    private void loadAlbum() {
        String[] parts = audioFolder.folderName.split("-");
        if (parts.length == 2) {
            subscription = MediaApplication.getInstance()
                    .getRepository().getAlbum(parts[0].trim(), parts[1].trim())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(data -> {
                        AppLog.ERROR("response: " + data.toString());
                    }, this::handleError);
        } else {
            showEnterCorrectArtistAlbum();
        }
    }

    private void handleError(Throwable throwable) {

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
}
