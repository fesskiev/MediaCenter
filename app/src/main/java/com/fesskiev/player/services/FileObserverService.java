package com.fesskiev.player.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.fesskiev.player.utils.fileobserver.FolderFileObserver;

public class FileObserverService extends Service {

    private static final String TAG = FileObserverService.class.getSimpleName();

    private FolderFileObserver folderFileObserver;

    public static void startFileObserverService(Context context) {
        Log.d(TAG, "startFileObserverService");
        Intent intent = new Intent(context, FileObserverService.class);
        context.startService(intent);
    }

    public static void stopFileObserverService(Context context) {
        Log.d(TAG, "stopFileObserverService");
        Intent intent = new Intent(context, FileObserverService.class);
        context.stopService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "create FileObserverService");
        folderFileObserver = new FolderFileObserver();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroy FileObserverService");
        folderFileObserver.stopWatching();

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
