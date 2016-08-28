package com.fesskiev.player.utils.fileobserver;


import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.utils.RxUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;

public class FolderFileObserver {

    private Subscription subscription;
    private static List<RecursiveFileObserver> recursiveFileObservers;

    public FolderFileObserver() {
        recursiveFileObservers = new ArrayList<>();

        subscription = MediaApplication
                .getInstance()
                .getMediaDataSource()
                .getFoldersPath()
                .subscribe(paths -> {
                    RxUtils.RxLog("create folder observer!");
                    if (paths != null) {
                        for (String path : paths) {
                            RecursiveFileObserver fileObserver = new RecursiveFileObserver(path);
                            fileObserver.startWatching();
                            recursiveFileObservers.add(fileObserver);
                        }
                    }
                });
    }


    public void stopWatching() {
        for (RecursiveFileObserver recursiveFileObserver : recursiveFileObservers) {
            recursiveFileObserver.stopWatching();
        }
        recursiveFileObservers = null;
        RxUtils.unsubscribe(subscription);
    }
}
