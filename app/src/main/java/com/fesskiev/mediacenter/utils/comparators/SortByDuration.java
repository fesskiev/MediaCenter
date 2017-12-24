package com.fesskiev.mediacenter.utils.comparators;


import com.fesskiev.mediacenter.data.model.AudioFile;

import java.util.Comparator;

public class SortByDuration implements Comparator<AudioFile> {

    @Override
    public int compare(AudioFile obj1, AudioFile obj2) {
        if (obj1.duration < obj2.duration) {
            return 1;
        }
        if (obj1.duration == obj2.duration) {
            return 0;
        }
        return -1;
    }
}
