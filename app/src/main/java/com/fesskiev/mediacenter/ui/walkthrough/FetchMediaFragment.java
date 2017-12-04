package com.fesskiev.mediacenter.ui.walkthrough;


import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.services.FileSystemService;
import com.fesskiev.mediacenter.ui.fetch.FetchContentViewModel;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentView;


public class FetchMediaFragment extends Fragment implements View.OnClickListener {

    public static FetchMediaFragment newInstance() {
        return new FetchMediaFragment();
    }

    private FetchContentView fetchContentView;

    private TextView fetchText;
    private Button[] buttons;

    private FileSystemService boundService;
    private boolean serviceBound;


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

        fetchContentView = view.findViewById(R.id.fetchContentView);
        observeData();
    }

    private void observeData() {
        FetchContentViewModel viewModel = ViewModelProviders.of(this).get(FetchContentViewModel.class);
        viewModel.getPrepareFetchLiveData().observe(this, Void -> {
            fetchContentView.fetchStart();
            fetchText.setVisibility(View.GONE);
            hideButtons();
        });
        viewModel.getFinishFetchLiveData().observe(this, Void -> {
            fetchContentView.fetchFinish();
            fetchMediaFilesSuccess();
        });
        viewModel.getPercentLiveData().observe(this, percent -> fetchContentView.setProgress(percent));
        viewModel.getFetchDescriptionLiveData().observe(this, description -> {
            String folderName = description.getFolderName();
            if (folderName != null) {
                fetchContentView.setFolderName(folderName);
            }
            String fileName = description.getFileName();
            if (fileName != null) {
                fetchContentView.setFileName(fileName);
            }
        });
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            FileSystemService.FileSystemLocalBinder binder = (FileSystemService.FileSystemLocalBinder) service;
            boundService = binder.getService();
            if (boundService.getScanState() == FileSystemService.SCAN_STATE.SCANNING) {
                hideButtons();
                fetchText.setVisibility(View.GONE);
                fetchContentView.fetchStart();
            } else if (boundService.getScanState() == FileSystemService.SCAN_STATE.FINISHED) {
                hideButtons();
                fetchMediaFilesSuccess();
                fetchContentView.fetchFinish();
            }
            serviceBound = true;
        }
    };

    private void bindFileSystemService() {
        Intent intent = new Intent(getActivity(), FileSystemService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindFileSystemService() {
        if (serviceBound) {
            getActivity().unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        bindFileSystemService();
    }

    @Override
    public void onStop() {
        super.onStop();
       unbindFileSystemService();
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
        FileSystemService.startFetchMedia(getContext());
    }

    private void stopFetchFiles() {
        FileSystemService.stopFileSystemService(getContext());
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
        return serviceBound && boundService.getScanState() == FileSystemService.SCAN_STATE.SCANNING;
    }

    public void stopFetchMedia() {
        if (isFetchMediaStart()) {
            stopFetchFiles();
        }
    }
}
