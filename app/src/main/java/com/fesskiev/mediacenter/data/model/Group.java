package com.fesskiev.mediacenter.data.model;

import android.content.Context;

import com.thoughtbot.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

import java.util.ArrayList;
import java.util.List;

public class Group extends MultiCheckExpandableGroup {

    private int iconResId;

    public Group(String title, List<GroupItem> items, int iconResId) {
        super(title, items);
        this.iconResId = iconResId;
    }

    public int getIconResId() {
        return iconResId;
    }

    private static Group makeExpandArtists(Context context, List<String> artists) {
        List<GroupItem> groupItems = new ArrayList<>();
        for (String artist : artists) {
            groupItems.add(new GroupItem(artist, GroupItem.TYPE.ARTIST));
        }

        return new Group("Artists", groupItems, 0);
    }

    private static Group makeExpandGenres(Context context, List<String> genres) {
        List<GroupItem> groupItems = new ArrayList<>();
        for (String genre : genres) {
            groupItems.add(new GroupItem(genre, GroupItem.TYPE.GENRE));
        }

        return new Group("Genre", groupItems, 0);
    }

    public static List<Group> makeGroups(Context context, List<String> genres, List<String> artists){
        List<Group> groups = new ArrayList<>();
        groups.add(Group.makeExpandGenres(context, genres));
        groups.add(Group.makeExpandArtists(context, artists));
        return groups;
    }
}