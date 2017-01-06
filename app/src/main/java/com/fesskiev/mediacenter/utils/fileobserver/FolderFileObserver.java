package com.fesskiev.mediacenter.utils.fileobserver;


import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.utils.RxUtils;

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
                .getRepository()
                .getFoldersPath()
                .first()
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
