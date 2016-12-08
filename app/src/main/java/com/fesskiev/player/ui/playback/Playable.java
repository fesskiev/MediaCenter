package com.fesskiev.player.ui.playback;


import com.fesskiev.player.data.model.MediaFile;

public interface Playable {

    void open(MediaFile mediaFile);

    void play();

    void pause();

    void next();

    void previous();

    boolean first();

    boolean last();

}
