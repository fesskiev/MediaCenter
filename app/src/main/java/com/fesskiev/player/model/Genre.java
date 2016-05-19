package com.fesskiev.player.model;


import android.database.Cursor;

import com.fesskiev.player.db.MediaCenterProvider;


public class Genre implements Comparable<Genre> {

    public String name;
    public String artworkPath;

    public Genre(Cursor cursor) {
        name = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_GENRE));
        artworkPath = cursor.getString(cursor.getColumnIndex(MediaCenterProvider.TRACK_COVER));
    }

    @Override
    public int compareTo(Genre another) {
        return this.name.compareTo(another.name);
    }

    @Override
    public String toString() {
        return "Genre{" +
                "name='" + name + '\'' +
                ", artworkPath='" + artworkPath + '\'' +
                '}';
    }
}
