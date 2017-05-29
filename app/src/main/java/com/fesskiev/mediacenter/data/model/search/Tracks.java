
package com.fesskiev.mediacenter.data.model.search;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Tracks {

    @SerializedName("track")
    @Expose
    private List<Track> track = null;

    public List<Track> getTrack() {
        return track;
    }

    public void setTrack(List<Track> track) {
        this.track = track;
    }

    @Override
    public String toString() {
        return "Tracks{" +
                "track=" + track +
                '}';
    }
}
