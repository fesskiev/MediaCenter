package com.fesskiev.player.ui.vk;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.player.R;
import com.fesskiev.player.model.vk.VKMusicFile;


public class VKPlayerFragment extends Fragment {

    private static final String TAG = VKPlayerFragment.class.getName();

    private static final String BUNDLE_VK_MUSIC_FILE = "com.fesskiev.BUNDLE_VK_MUSIC_FILE";
    private static final String BUNDLE_FILE_PATH = "com.fesskiev.BUNDLE_FILE_PATH";

    private VKMusicFile vkMusicFile;
    private String filePath;


    public static VKPlayerFragment newInstance(VKMusicFile vkMusicFile, String filePath) {
        VKPlayerFragment fragment = new VKPlayerFragment();
        Bundle args = new Bundle();
        args.putParcelable(BUNDLE_VK_MUSIC_FILE, vkMusicFile);
        args.putString(BUNDLE_FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vkMusicFile = getArguments().getParcelable(BUNDLE_VK_MUSIC_FILE);
            filePath = getArguments().getString(BUNDLE_FILE_PATH);
            Log.d(TAG, "file path: " + filePath + " music file: " + vkMusicFile.toString());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vkplayer, container, false);
    }

}
