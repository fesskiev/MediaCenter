package com.fesskiev.player.data.model;


import android.database.Cursor;

import com.fesskiev.player.data.source.local.db.DatabaseHelper;


public class Genre implements Comparable<Genre> {

    public String name;
    public String artworkPath;

    public Genre(Cursor cursor) {
        name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRACK_GENRE));
        artworkPath = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TRACK_COVER));
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
