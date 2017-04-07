package com.fesskiev.mediacenter.utils;


import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class StorageUtils {

    private static final String TAG = StorageUtils.class.getSimpleName();

    public static class StorageInfo {

        public final String path;
        public final boolean readonly;
        public final boolean removable;
        public final int number;

        StorageInfo(String path, boolean readonly, boolean removable, int number) {
            this.path = path;
            this.readonly = readonly;
            this.removable = removable;
            this.number = number;
        }

        public String getDisplayName() {
            StringBuilder res = new StringBuilder();
            if (!removable) {
                res.append("Internal SD card");
            } else if (number > 1) {
                res.append("SD card " + number);
            } else {
                res.append("SD card");
            }
            if (readonly) {
                res.append(" (Read only)");
            }
            return res.toString();
        }
    }

    public static List<StorageInfo> getStorageList() {
        List<StorageInfo> list = new ArrayList<>();
        String defPath = Environment.getExternalStorageDirectory().getPath();
        boolean defPathRemovable = Environment.isExternalStorageRemovable();
        String defPathState = Environment.getExternalStorageState();
        boolean defPathAvailable = defPathState.equals(Environment.MEDIA_MOUNTED)
                || defPathState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean defPathReadonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

        HashSet<String> paths = new HashSet<>();
        int curRemovableNumber = 1;

        if (defPathAvailable) {
            paths.add(defPath);
            list.add(0, new StorageInfo(defPath, defPathReadonly,
                    defPathRemovable, defPathRemovable ? curRemovableNumber++ : -1));
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            Log.d(TAG, "/proc/mounts");
            while ((line = bufferedReader.readLine()) != null) {
                Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mountPoint = tokens.nextToken(); //mount point
                    if (paths.contains(mountPoint)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {
                            paths.add(mountPoint);
                            list.add(new StorageInfo(mountPoint, readonly, true, curRemovableNumber++));
                        }
                    }
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return list;
    }
}
