package com.fesskiev.mediacenter.ui.playback;


import com.fesskiev.mediacenter.data.model.MediaFile;

public interface Playable {

    void open(MediaFile mediaFile);

    void play();

    void pause();

    void next();

    void previous();

    boolean first();

    boolean last();

}
