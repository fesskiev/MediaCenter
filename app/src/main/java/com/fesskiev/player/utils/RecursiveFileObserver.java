package com.fesskiev.player.utils;


import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RecursiveFileObserver extends FileObserver {

    private List<SingleFileObserver> observers;
    private String path;
    private int mask;

    public RecursiveFileObserver(String path) {
        this(path, ALL_EVENTS);
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
                if (files[i].isDirectory() && !files[i].getName().equals(".")
                        && !files[i].getName().equals("..")) {
                    stack.push(files[i].getPath());
                }
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
        Log.d("observer", "path: " + path);
        event &= FileObserver.ALL_EVENTS;
        switch (event) {
            case FileObserver.DELETE_SELF:
                Log.d("observer", "delete self");
                break;
            case FileObserver.CREATE:
                Log.d("observer", "event create");
                break;
            case FileObserver.DELETE:
                Log.d("observer", "event delete");
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
        public void onEvent(int event, String path) {
            String newPath = this.path + "/" + path;
            RecursiveFileObserver.this.onEvent(event, newPath);
        }
    }
}