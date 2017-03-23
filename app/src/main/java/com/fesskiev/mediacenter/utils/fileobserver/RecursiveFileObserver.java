package com.fesskiev.mediacenter.utils.fileobserver;


import android.os.FileObserver;
import android.util.Log;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.utils.CacheManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecursiveFileObserver extends FileObserver {

    private static final String TAG = RecursiveFileObserver.class.getName();

    private List<SingleFileObserver> observers;
    private String path;
    private int mask;

    public RecursiveFileObserver(String path) {
        this(path, DELETE | CREATE | DELETE_SELF);
    }

    public RecursiveFileObserver(String path, int mask) {
        super(path, mask);
        this.path = path;
        this.mask = mask;
    }

    @Override
    public void startWatching() {
        if (observers != null) {
            return;
        }
        observers = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        stack.push(path);

        while (!stack.empty()) {
            String parent = stack.pop();
            observers.add(new SingleFileObserver(parent, mask));
            File path = new File(parent);
            File[] files = path.listFiles();
            if (files == null) {
                continue;
            }
            for (int i = 0; i < files.length; ++i) {
                stack.push(files[i].getPath());
            }
        }

        for (int i = 0; i < observers.size(); i++) {
            observers.get(i).startWatching();
        }
    }

    @Override
    public void stopWatching() {
        if (observers == null) {
            return;
        }
        for (int i = 0; i < observers.size(); ++i) {
            observers.get(i).stopWatching();
        }

        observers.clear();
        observers = null;
    }

    @Override
    public void onEvent(int event, String path) {
        switch (event) {
            case FileObserver.DELETE_SELF:
                Log.d(TAG, "delete self: " + path);
                break;
            case FileObserver.CREATE:
                Log.d(TAG, "event create: " + path);
                if (CacheManager.CHECK_DOWNLOADS_FOLDER_PATH.equals(path)) {

                }
                break;
            case FileObserver.DELETE:
                Log.d(TAG, "event delete: " + path);
                break;
        }
    }

    private class SingleFileObserver extends FileObserver {
        private String path;

        public SingleFileObserver(String path, int mask) {
            super(path, mask);
            this.path = path;
        }

        @Override
        public void onEvent(int event, String p) {
            RecursiveFileObserver.this.onEvent(event, path);
        }
    }
}