package com.fesskiev.mediacenter.utils.comparators;


import com.fesskiev.mediacenter.data.model.AudioFile;

import java.util.Comparator;

public class SortByTimestamp implements Comparator<AudioFile> {

    @Override
    public int compare(AudioFile obj1, AudioFile obj2) {
        if (obj1.timestamp < obj2.timestamp) {
            return 1;
        }
        if (obj1.timestamp == obj2.timestamp) {
            return 0;
        }
        return -1;
    }
}
