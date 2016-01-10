package com.fesskiev.player.utils;


import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileHelper {

    private final static String TAG = DownloadFileHelper.class.getSimpleName();

    public interface OnFileDownloadListener {
        void fileDownloadComplete(String filePath);

        void fileDownloadFailed();

        void fileDownloadProgress(double progress);
    }

    private OnFileDownloadListener listener;
    private String url;
    private String fileName;
    private String filePath;


    public DownloadFileHelper(String url, String fileName) {
        this.url = url;
        this.fileName = fileName;
        downloadStream();
    }

    public void setOnFileDownloadListener(OnFileDownloadListener l) {
        this.listener = l;
    }

    private void downloadFromUrl(URL url, String localFilename) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            URLConnection urlConnection = url.openConnection();
            is = urlConnection.getInputStream();
            int size = urlConnection.getContentLength();
            fos = new FileOutputStream(localFilename);

            byte[] buffer = new byte[4096];
            int count;
            double totalCount = 0d;

            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);

                if (size > 0 && listener != null) {
                    totalCount += count;
                    double progress = (totalCount / size) * 100.0;

                    listener.fileDownloadProgress(progress);
//                    Log.d(TAG, "progress: " + progress + "%");
                }
            }
            if (listener != null) {
                listener.fileDownloadComplete(filePath);
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    private String getFilePath() {
        String externalStorage = Environment.getExternalStorageDirectory().toString();
        File folder = new File(externalStorage + "/MusicPlayer/Downloads/");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return new File(folder.getAbsolutePath(), fileName + ".mp3").getAbsolutePath();
    }

    public void downloadStream() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    filePath = getFilePath();
                    Log.d(TAG, "file path: " + filePath);

                    downloadFromUrl(new URL(url), filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    if (listener != null) {
                        listener.fileDownloadFailed();
                    }
                }
            }
        }).start();
    }
}
