package com.fesskiev.player.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.fesskiev.player.utils.fileobserver.FolderFileObserver;

public class FileObserverService extends Service {

    private FolderFileObserver folderFileObserver;

    public static void startFileObserverService(Context context) {
        Intent intent = new Intent(context, FileObserverService.class);
        context.startService(intent);
    }

    public static void stopFileObserverService(Context context) {
        Intent intent = new Intent(context, FileObserverService.class);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        folderFileObserver = new FolderFileObserver(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        folderFileObserver.stopWatching();

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
