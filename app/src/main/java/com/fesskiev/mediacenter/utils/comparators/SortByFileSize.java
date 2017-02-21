package com.fesskiev.mediacenter.utils.comparators;

import com.fesskiev.mediacenter.data.model.AudioFile;

import java.util.Comparator;


public class SortByFileSize implements Comparator<AudioFile> {

    @Override
    public int compare(AudioFile obj1, AudioFile obj2) {
        if (obj1.size < obj2.size) {
            return 1;
        }
        if (obj1.size == obj2.size) {
            return 0;
        }
        return -1;
    }
}
