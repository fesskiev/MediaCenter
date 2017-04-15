package com.fesskiev.mediacenter.utils.converter;


import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.data.model.AudioFile;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;

public class AudioConverterHelper {

    public interface OnConverterLibraryLoadListener {

        void onSuccess();

        void onFailure();
    }

    public interface OnConvertProcessListener {

        void onStart();

        void onSuccess(AudioFile audioFile);

        void onFailure(Exception error);
    }


    private static AudioConverterHelper INSTANCE;

    private Context context;
    private boolean libraryLoaded;

    public static AudioConverterHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AudioConverterHelper();
        }
        return INSTANCE;
    }

    private AudioConverterHelper() {
        context = MediaApplication.getInstance().getApplicationContext();
    }

    public void loadFFmpegLibrary(final OnConverterLibraryLoadListener listener) {
        try {
            FFmpeg.getInstance(context).loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess() {
                    libraryLoaded = true;
                    listener.onSuccess();
                }

                @Override
                public void onFailure() {
                    libraryLoaded = false;
                    listener.onFailure();
                }

                @Override
                public void onFinish() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            libraryLoaded = false;
            listener.onFailure();
        }
    }

    private void convertAudio(AudioFile audioFile, AudioFormat format, OnConvertProcessListener listener) {
        File file = audioFile.filePath;
        if (!libraryLoaded) {
            listener.onFailure(new Exception("FFmpeg not loaded"));
            return;
        }
        if (!audioFile.exists()) {
            listener.onFailure(new IOException("File not exists"));
            return;
        }
        if (!file.canRead()) {
            listener.onFailure(new IOException("Can't read the file. Missing permission?"));
            return;
        }
        final File convertedFile = getConvertedFile(file, format);

        final String[] cmd = new String[]{"-y", "-i", file.getPath(), convertedFile.getPath()};
        try {
            FFmpeg.getInstance(context).execute(cmd, new FFmpegExecuteResponseHandler() {
                @Override
                public void onStart() {
                    listener.onStart();
                }

                @Override
                public void onProgress(String message) {

                }

                @Override
                public void onSuccess(String message) {
                    audioFile.convertedPath = convertedFile;
                    listener.onSuccess(audioFile);
                }

                @Override
                public void onFailure(String message) {
                    listener.onFailure(new IOException(message));
                }

                @Override
                public void onFinish() {

                }
            });
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }

    public void convertAudioIfNeed(AudioFile audioFile, OnConvertProcessListener listener) {
        MediaApplication.getInstance().getRepository().getFolderFilePaths("Temp")
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(paths -> {
                    String path = needConvert(paths, audioFile);
                    if (TextUtils.isEmpty(path)) {
                        convertAudio(audioFile, AudioFormat.WAV, listener);
                    } else {
                        listener.onSuccess(audioFile);
                    }
                });
    }


    public static boolean isAudioFileFLAC(AudioFile audioFile) {
        String path = audioFile.getFilePath();
        String extension = path.substring(path.lastIndexOf("."));
        return extension.equalsIgnoreCase(".flac");
    }

    private String getPathWithoutExtension(String path) {
        int pos = path.lastIndexOf(".");
        if (pos > 0) {
            return path.substring(0, pos);
        }
        return "";
    }

    private boolean twoPathEquals(String first, String second) {
        return getPathWithoutExtension(first).equals(getPathWithoutExtension(second));
    }

    private String needConvert(List<String> paths, AudioFile audioFile) {
        for (String path : paths) {
            if (twoPathEquals(path, audioFile.getFilePath())) {
                return path;
            }
        }
        return "";
    }

    private File getConvertedFile(File originalFile, AudioFormat format) {
        File temp = new File(Environment.getExternalStorageDirectory().toString() + "/MediaCenter/Temp/");
        if (!temp.exists()) {
            temp.mkdirs();
        }
        String[] f = originalFile.getName().split("\\.");
        String fileName = originalFile.getName().replace(f[f.length - 1], format.getFormat());

        return new File(temp.getAbsolutePath(), fileName);
    }

}

