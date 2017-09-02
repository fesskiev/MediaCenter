package com.fesskiev.mediacenter.ui.walkthrough;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.utils.FetchMediaFilesManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentView;


public class FetchMediaFragment extends Fragment implements View.OnClickListener {

    public static FetchMediaFragment newInstance() {
        return new FetchMediaFragment();
    }

    private FetchMediaFilesManager fetchMediaFilesManager;

    private TextView fetchText;
    private Button[] buttons;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileSystemService.startFileSystemService(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fetch_media, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchText = view.findViewById(R.id.fetchText);

        buttons = new Button[]{
                view.findViewById(R.id.fetchMediaButton),
                view.findViewById(R.id.fetchMediaSkipButton)
        };

        for (Button button : buttons) {
            button.setOnClickListener(this);
        }

        FetchContentView fetchContentView = view.findViewById(R.id.fetchContentView);

        fetchMediaFilesManager = FetchMediaFilesManager.getInstance();
        fetchMediaFilesManager.setFetchContentView(fetchContentView);
        fetchMediaFilesManager.setTextWhite();
        fetchMediaFilesManager.setOnFetchMediaFilesListener(new FetchMediaFilesManager.OnFetchMediaFilesListener() {

            @Override
            public void onFetchMediaPrepare() {
                fetchText.setVisibility(View.GONE);
                hideButtons();
            }

            @Override
            public void onFetchAudioContentStart(boolean clear) {

            }

            @Override
            public void onFetchVideoContentStart(boolean clear) {

            }

            @Override
            public void onFetchMediaContentFinish() {
                fetchMediaFilesSuccess();
            }

            @Override
            public void onAudioFolderCreated() {

            }

            @Override
            public void onVideoFolderCreated() {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMediaFilesManager.register();
        if (fetchMediaFilesManager.isFetchStart()) {
            fetchText.setVisibility(View.GONE);
            fetchMediaFilesManager.visibleContent();
            hideButtons();
        } else if (fetchMediaFilesManager.isFetchComplete()) {
            hideButtons();
            fetchMediaFilesSuccess();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        fetchMediaFilesManager.unregister();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fetchMediaButton:
                fetchMediaFiles();
                break;
            case R.id.fetchMediaSkipButton:
                skipFetchMediaFiles();
                break;
        }
    }

    private void fetchMediaFiles() {
        FileSystemService.startFetchMedia(getContext().getApplicationContext());
    }

    private void skipFetchMediaFiles() {
        hideButtons();
        fetchMediaFilesSuccess();
        notifyFetchMediaGranted();
    }


    private void fetchMediaFilesSuccess() {
        fetchText.setVisibility(View.VISIBLE);
        fetchText.setText(getString(R.string.search_media_files_success));
        fetchText.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.icon_walk_through_ok, 0, 0);

        notifyFetchMediaGranted();
    }

    private void notifyFetchMediaGranted() {
        WalkthroughFragment walkthroughFragment = (WalkthroughFragment) getFragmentManager().
                findFragmentByTag(WalkthroughFragment.class.getName());
        if (walkthroughFragment != null) {
            walkthroughFragment.fetchMediaGranted();
        }
    }

    private void hideButtons() {
        for (Button button : buttons) {
            button.setVisibility(View.GONE);
        }
    }

    public boolean isFetchMediaStart() {
        return fetchMediaFilesManager.isFetchStart();
    }

    private void stopFetchFiles() {
        FileSystemService.stopFileSystemService(getContext().getApplicationContext());
    }

    public void stopFetchMedia() {
        if (fetchMediaFilesManager.isFetchStart()) {
            stopFetchFiles();
        }
        fetchMediaFilesManager.unregister();
    }
}
