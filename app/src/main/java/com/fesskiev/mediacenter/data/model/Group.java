package com.fesskiev.mediacenter.data.model;

import android.content.Context;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.audio.CONTENT_TYPE;
import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class Group extends MultiCheckExpandableGroup {

    public static final int GROUP_GENRE = 0;
    public static final int GROUP_ARTIST = 1;

    private int iconResId;
    private int id;

    public Group(String title, List<GroupItem> items, int iconResId, int id) {
        super(title, items);
        this.iconResId = iconResId;
        this.id = id;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getId() {
        return id;
    }

    private static Group makeExpandArtists(Context context, List<String> artists) {
        List<GroupItem> groupItems = new ArrayList<>();
        for (String artist : artists) {
            groupItems.add(new GroupItem(artist, CONTENT_TYPE.ARTIST));
        }

        return new Group(context.getString(R.string.group_artists), groupItems, R.drawable.icon_artist, GROUP_ARTIST);
    }

    private static Group makeExpandGenres(Context context, List<String> genres) {
        List<GroupItem> groupItems = new ArrayList<>();
        for (String genre : genres) {
            groupItems.add(new GroupItem(genre, CONTENT_TYPE.GENRE));
        }

        return new Group(context.getString(R.string.group_genres), groupItems, R.drawable.icon_genre, GROUP_GENRE);
    }

    public static List<Group> makeGroups(Context context, List<String> genres, List<String> artists){
        List<Group> groups = new ArrayList<>();
        groups.add(Group.makeExpandGenres(context, genres));
        groups.add(Group.makeExpandArtists(context, artists));
        return groups;
    }
}