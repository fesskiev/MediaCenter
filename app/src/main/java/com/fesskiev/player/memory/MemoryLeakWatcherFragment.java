package com.fesskiev.player.memory;

import android.support.v4.app.Fragment;

import com.fesskiev.player.MediaApplication;
import com.squareup.leakcanary.RefWatcher;


public class MemoryLeakWatcherFragment extends Fragment {

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MediaApplication.getInstance().getRefWatcher();
        refWatcher.watch(this);
    }
}
