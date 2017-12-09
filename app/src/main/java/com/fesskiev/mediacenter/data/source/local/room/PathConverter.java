package com.fesskiev.mediacenter.data.source.local.room;


import android.arch.persistence.room.TypeConverter;

import java.io.File;

public class PathConverter {

    @TypeConverter
    public File stringPathToFile(String path) {
        return path == null ? null : new File(path);
    }

    @TypeConverter
    public String filePathToString(File path) {
        return path == null ? null : path.getAbsolutePath();
    }
}
