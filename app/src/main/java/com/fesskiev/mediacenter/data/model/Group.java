package com.fesskiev.mediacenter.data.model;

import android.content.Context;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.audio.CONTENT_TYPE;
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
            groupItems.add(new GroupItem(artist, CONTENT_TYPE.ARTIST));
        }

        return new Group(context.getString(R.string.group_artists), groupItems, R.drawable.icon_artist);
    }

    private static Group makeExpandGenres(Context context, List<String> genres) {
        List<GroupItem> groupItems = new ArrayList<>();
        for (String genre : genres) {
            groupItems.add(new GroupItem(genre, CONTENT_TYPE.GENRE));
        }

        return new Group(context.getString(R.string.group_genres), groupItems, R.drawable.icon_genre);
    }

    public static List<Group> makeGroups(Context context, List<String> genres, List<String> artists){
        List<Group> groups = new ArrayList<>();
        groups.add(Group.makeExpandGenres(context, genres));
        groups.add(Group.makeExpandArtists(context, artists));
        return groups;
    }
}