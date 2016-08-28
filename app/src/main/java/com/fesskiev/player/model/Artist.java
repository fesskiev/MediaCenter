package com.fesskiev.player.model;


import android.database.Cursor;

import com.fesskiev.player.db.MediaDatabaseHelper;

public class Artist implements Comparable<Artist> {

    public String name;
    public String artworkPath;

    public Artist(Cursor cursor) {
        name = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_ARTIST));
        artworkPath = cursor.getString(cursor.getColumnIndex(MediaDatabaseHelper.TRACK_COVER));
    }

    @Override
    public int compareTo(Artist another) {
        return this.name.compareTo(another.name);
    }

    @Override
    public String toString() {
        return "Artist{" +
                "name='" + name + '\'' +
                ", artworkPath='" + artworkPath + '\'' +
                '}';
    }
}
