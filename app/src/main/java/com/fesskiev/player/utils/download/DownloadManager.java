package com.fesskiev.player.utils.download;


import android.os.Environment;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadManager implements Runnable {

    public interface OnDownloadListener {
        void onStatusChanged();

        void onProgress();
    }

    private static final int MAX_BUFFER_SIZE = 1024;

    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;

    private OnDownloadListener listener;
    private URL url;
    private String fileName;
    private int size;
    private int downloaded;
    private int status;
    private int updateCount;

    public DownloadManager(String url, String fleName) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        this.fileName = fleName;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;

        download();
    }

    public void setOnDownloadListener(OnDownloadListener listener) {
        this.listener = listener;
    }

    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void pause() {
        status = PAUSED;
        stateChanged();
    }

    public void resume() {
        status = DOWNLOADING;
        download();
        stateChanged();
    }

    public void cancel() {
        status = CANCELLED;
        stateChanged();
    }

    private void error() {
        status = ERROR;
        stateChanged();
    }

    public int getSize() {
        return size;
    }

    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }

    public int getStatus() {
        return status;
    }


    private File getFilePath() {
        String externalStorage = Environment.getExternalStorageDirectory().toString();
        File folder = new File(externalStorage + "/MediaCenter/Downloads/");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        return new File(folder.getAbsolutePath(), fileName + ".mp3");
    }

    public boolean removeFile(){
        return getFilePath().delete();
    }

    private void stateChanged() {
        if (listener != null) {
            listener.onStatusChanged();
        }
    }

    private void progressChanged() {
        if (listener != null) {
            listener.onProgress();
        }
    }

    @Override
    public void run() {
        RandomAccessFile file = null;
        InputStream stream = null;

        try {

            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Range",
                    "bytes=" + downloaded + "-");

            connection.connect();
            if (connection.getResponseCode() / 100 != 2) {
                error();
            }

            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error();
            }

            if (size == -1) {
                size = contentLength;
            }

            file = new RandomAccessFile(getFilePath(), "rw");
            file.seek(downloaded);

            stream = connection.getInputStream();
            while (status == DOWNLOADING) {
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }

                int read = stream.read(buffer);
                if (read == -1)
                    break;

                file.write(buffer, 0, read);
                downloaded += read;

                updateCount++;
                if (updateCount == 500) {
                    progressChanged();
                    updateCount = 0;
                }
            }

            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            error();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
