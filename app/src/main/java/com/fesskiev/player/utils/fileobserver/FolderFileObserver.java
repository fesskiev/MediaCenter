package com.fesskiev.player.utils.fileobserver;


import android.content.Context;

import com.fesskiev.player.db.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class FolderFileObserver {

    private Context context;
    private static List<RecursiveFileObserver> recursiveFileObservers;

    public FolderFileObserver(Context context) {
        this.context = context;
        recursiveFileObservers = new ArrayList<>();

        new Thread(new FolderObserver()).start();
    }


    private class FolderObserver implements Runnable {

        @Override
        public void run() {

            List<String> paths = DatabaseHelper.getFoldersPath(context);
            for (String path : paths) {
                RecursiveFileObserver fileObserver = new RecursiveFileObserver(context, path);
                fileObserver.startWatching();
                recursiveFileObservers.add(fileObserver);
            }
        }
    }

    public void stopWatching() {
        for (RecursiveFileObserver recursiveFileObserver : recursiveFileObservers) {
            recursiveFileObserver.stopWatching();
        }
        recursiveFileObservers = null;
    }
}
