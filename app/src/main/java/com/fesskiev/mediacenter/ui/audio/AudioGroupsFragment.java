package com.fesskiev.mediacenter.ui.audio;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.Group;
import com.fesskiev.mediacenter.data.model.GroupItem;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.view.animation.Animation.RELATIVE_TO_SELF;


public class AudioGroupsFragment extends Fragment implements AudioContent {

    public static AudioGroupsFragment newInstance() {
        return new AudioGroupsFragment();
    }


    private RecyclerView recyclerView;

    private Subscription subscription;
    private DataRepository repository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = MediaApplication.getInstance().getRepository();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_groups, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    public void onResume() {
        super.onResume();
        fetch();
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.unsubscribe(subscription);
    }


    @Override
    public void fetch() {
        subscription = Observable.zip(repository.getGenresList(), repository.getArtistsList(),
                (genres, artists) -> Group.makeGroups(getContext(), genres, artists))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::makeExpandAdapter,
                        throwable -> {

                        });
    }

    @Override
    public void clear() {

    }

    private void makeExpandAdapter(List<Group> groups) {
        GroupsAdapter adapter = new GroupsAdapter(groups);
        recyclerView.setAdapter(adapter);
        adapter.setChildClickListener((v, checked, group, childIndex) -> processClick(group, childIndex));
    }

    private void processClick(CheckedExpandableGroup group, int childIndex) {
        final GroupItem groupItem = (GroupItem) group.getItems().get(childIndex);
        if (groupItem != null) {
            AppLog.ERROR("group item: " + groupItem.toString());
            switch (groupItem.getType()) {
                case ARTIST:
                    String artist = groupItem.getName();
                    break;
                case GENRE:
                    String genre = groupItem.getName();
                    break;
                default:
                    break;
            }
        }
    }


    public class GroupsAdapter extends CheckableChildRecyclerViewAdapter<GroupsAdapter.GroupViewHolder, GroupsAdapter.GroupItemViewHolder> {


        public class GroupItemViewHolder extends CheckableChildViewHolder {

            private CheckedTextView groupItemName;

            public GroupItemViewHolder(View itemView) {
                super(itemView);
                groupItemName = (CheckedTextView) itemView.findViewById(R.id.itemName);
            }

            public void setGroupItemName(String name) {
                groupItemName.setText(name);
            }

            @Override
            public Checkable getCheckable() {
                return groupItemName;
            }

        }

        public class GroupViewHolder extends com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder {

            private TextView genreName;
            private ImageView arrow;
            private ImageView icon;

            public GroupViewHolder(View itemView) {
                super(itemView);
                genreName = (TextView) itemView.findViewById(R.id.itemGroupName);
                arrow = (ImageView) itemView.findViewById(R.id.itemArrow);
                icon = (ImageView) itemView.findViewById(R.id.itemGroupIcon);
            }

            public void setGroupTitle(ExpandableGroup group) {
                genreName.setText(group.getTitle());
                icon.setBackgroundResource(((Group) group).getIconResId());
            }

            @Override
            public void expand() {
                animateExpand();
            }

            @Override
            public void collapse() {
                animateCollapse();
            }

            private void animateExpand() {
                RotateAnimation rotate =
                        new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(300);
                rotate.setFillAfter(true);
                arrow.setAnimation(rotate);
            }

            private void animateCollapse() {
                RotateAnimation rotate =
                        new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(300);
                rotate.setFillAfter(true);
                arrow.setAnimation(rotate);
            }
        }


        public GroupsAdapter(List<Group> groups) {
            super(groups);
        }

        @Override
        public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_group, parent, false);
            return new GroupViewHolder(view);
        }


        @Override
        public void onBindGroupViewHolder(GroupViewHolder holder, int flatPosition,
                                          ExpandableGroup group) {

            holder.setGroupTitle(group);
        }

        @Override
        public GroupItemViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_group_item, parent, false);
            return new GroupItemViewHolder(view);
        }

        @Override
        public void onBindCheckChildViewHolder(GroupItemViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
            final GroupItem groupItem = (GroupItem) group.getItems().get(childIndex);
            holder.setGroupItemName(groupItem.getName());
        }
    }


}
