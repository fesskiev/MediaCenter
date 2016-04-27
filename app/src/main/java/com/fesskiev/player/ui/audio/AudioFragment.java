package com.fesskiev.player.ui.audio;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.db.MediaCenterProvider;
import com.fesskiev.player.model.AudioFolder;
import com.fesskiev.player.services.FileObserverService;
import com.fesskiev.player.ui.ViewPagerFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AudioFragment extends ViewPagerFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int GET_AUDIO_FOLDERS_LOADER = 1001;
    public static final String EXTRA_IS_FIRST_START = "com.fesskiev.player.EXTRA_IS_FIRST_START";

    public static AudioFragment newInstance(boolean isFirstStart) {
        AudioFragment fragment = new AudioFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_FIRST_START, isFirstStart);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boolean isFirstStart = getArguments().getBoolean(EXTRA_IS_FIRST_START);
            if (!isFirstStart) {
                fetchAudioFolders();
            }
        }
    }

    private void fetchAudioFolders() {
        getActivity().getSupportLoaderManager().initLoader(GET_AUDIO_FOLDERS_LOADER, null, this);
    }

    @Override
    public String[] getTitles() {
        return new String[]{
                getString(R.string.audio_tab_title_folders),
                getString(R.string.audio_tab_title_genres),
                getString(R.string.audio_tab_title_tracks)
        };
    }

    @Override
    public int[] getImagesIds() {
        return new int[]{
                R.drawable.icon_audio,
                R.drawable.icon_audio,
                R.drawable.icon_audio
        };
    }

    @Override
    public Fragment[] getPagerFragments() {
        return new Fragment[]{
                AudioFoldersFragment.newInstance(),
                GenresFragment.newInstance(),
                TracksFragment.newInstance()
        };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GET_AUDIO_FOLDERS_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaCenterProvider.AUDIO_FOLDERS_TABLE_CONTENT_URI,
                        null,
                        null,
                        null,
                        null

                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.getCount() > 0) {
            List<AudioFolder> audioFolders = new ArrayList<>();

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                AudioFolder audioFolder = new AudioFolder(cursor);
                audioFolders.add(audioFolder);
            }

            MediaApplication.getInstance().getAudioPlayer().audioFolders = audioFolders;

            updateAudioFoldersFragment(audioFolders);
        }
        cursor.close();

        FileObserverService.startFileObserverService(getActivity());
    }

    private void updateAudioFoldersFragment(final List<AudioFolder> audioFolders) {
        AudioFragment audioFragment = (AudioFragment) getFragmentManager().
                findFragmentByTag(AudioFragment.class.getName());
        if (audioFragment != null) {
            List<Fragment> registeredFragments = audioFragment.getRegisteredFragments();
            if (registeredFragments != null) {
                for (Fragment fragment : registeredFragments) {
                    if (fragment instanceof AudioFoldersFragment) {
                        final AudioFoldersFragment audioFoldersFragment = (AudioFoldersFragment) fragment;
                        Collections.sort(audioFolders);
                        ((AudioFoldersFragment.AudioFoldersAdapter)
                                audioFoldersFragment.getAdapter()).refresh(audioFolders);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
