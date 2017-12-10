package com.fesskiev.mediacenter.ui.audio;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.Group;
import com.fesskiev.mediacenter.data.model.GroupItem;
import com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity;
import com.fesskiev.mediacenter.utils.AppAnimationUtils;
import com.thoughtbot.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import com.thoughtbot.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import com.thoughtbot.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;
import com.thoughtbot.expandablerecyclerview.listeners.GroupExpandCollapseListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity.EXTRA_CONTENT_TYPE;
import static com.fesskiev.mediacenter.ui.audio.tracklist.TrackListActivity.EXTRA_CONTENT_TYPE_VALUE;


public class AudioGroupsFragment extends Fragment implements AudioContent {

    public static AudioGroupsFragment newInstance() {
        return new AudioGroupsFragment();
    }

    private final static String BUNDLE_EXPANDED_IDS = "com.fesskiev.player.BUNDLE_EXPANDED_IDS";

    @Inject
    AppAnimationUtils animationUtils;

    private RecyclerView recyclerView;
    private GroupsAdapter adapter;

    private ArrayList<Integer> expandedGroupIds;

    private AudioGroupsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MediaApplication.getInstance().getAppComponent().inject(this);
        if (savedInstanceState == null) {
            expandedGroupIds = new ArrayList<>();
            expandedGroupIds.add(Group.GROUP_ARTIST);
            expandedGroupIds.add(Group.GROUP_GENRE);
        } else {
            expandedGroupIds = savedInstanceState.getIntegerArrayList(BUNDLE_EXPANDED_IDS);
        }
        observeData();
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

        recyclerView = view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new NpaLinearLayoutManager(getContext()));
    }

    private void observeData() {
        viewModel = ViewModelProviders.of(this).get(AudioGroupsViewModel.class);
        viewModel.getGroupsLiveData().observe(this, this::makeExpandAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        fetch();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(BUNDLE_EXPANDED_IDS, expandedGroupIds);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void fetch() {
        viewModel.getGroups();
    }


    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        List<CheckedExpandableGroup> groups = (List<CheckedExpandableGroup>) adapter.getGroups();
        if (groups != null && !groups.isEmpty()) {
            for (CheckedExpandableGroup group : groups) {
                group.getItems().clear();
            }
        }
        adapter.clearChoices();
        adapter.notifyDataSetChanged();
    }

    private void makeExpandAdapter(List<Group> groups) {
        adapter = new GroupsAdapter(groups);
        recyclerView.setAdapter(adapter);
        adapter.setChildClickListener((v, checked, group, childIndex) -> processClick(group, childIndex));
        adapter.setOnGroupExpandCollapseListener(new GroupExpandCollapseListener() {
            @Override
            public void onGroupExpanded(ExpandableGroup group) {
                addExpandedId(((Group) group).getId());
            }

            @Override
            public void onGroupCollapsed(ExpandableGroup group) {
                removeExpandedId(((Group) group).getId());
            }
        });
        toggleGroups();
    }

    private void removeExpandedId(Integer id) {
        if (expandedGroupIds.contains(id)) {
            expandedGroupIds.remove(id);
        }
    }

    private void addExpandedId(Integer id) {
        if (!expandedGroupIds.contains(id)) {
            expandedGroupIds.add(id);
        }
    }

    private void toggleGroups() {
        for (int i = 0; i < expandedGroupIds.size(); i++) {
            int id = expandedGroupIds.get(i);
            adapter.toggleGroup(id);
        }
    }

    private void processClick(CheckedExpandableGroup group, int childIndex) {
        final GroupItem groupItem = (GroupItem) group.getItems().get(childIndex);
        if (groupItem != null) {
            switch (groupItem.getType()) {
                case ARTIST:
                    String artist = groupItem.getName();
                    Intent i = new Intent(getContext(), TrackListActivity.class);
                    i.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE.ARTIST);
                    i.putExtra(EXTRA_CONTENT_TYPE_VALUE, artist);
                    startActivity(i);
                    break;
                case GENRE:
                    String genre = groupItem.getName();
                    Intent intent = new Intent(getContext(), TrackListActivity.class);
                    intent.putExtra(EXTRA_CONTENT_TYPE, CONTENT_TYPE.GENRE);
                    intent.putExtra(EXTRA_CONTENT_TYPE_VALUE, genre);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private static class NpaLinearLayoutManager extends LinearLayoutManager {
        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public NpaLinearLayoutManager(Context context) {
            super(context);
        }
    }


    private class GroupsAdapter extends CheckableChildRecyclerViewAdapter<GroupsAdapter.CustomGroupViewHolder,
            GroupsAdapter.GroupItemViewHolder> {


        public class GroupItemViewHolder extends CheckableChildViewHolder {

            private CheckedTextView groupItemName;

            public GroupItemViewHolder(View itemView) {
                super(itemView);
                groupItemName = itemView.findViewById(R.id.itemName);
            }

            public void setGroupItemName(String name) {
                groupItemName.setText(name);
            }

            @Override
            public Checkable getCheckable() {
                return groupItemName;
            }

        }

        public class CustomGroupViewHolder extends GroupViewHolder {

            private TextView genreName;
            private ImageView arrow;
            private ImageView icon;

            public CustomGroupViewHolder(View itemView) {
                super(itemView);
                genreName = itemView.findViewById(R.id.itemGroupName);
                arrow = itemView.findViewById(R.id.itemArrow);
                icon = itemView.findViewById(R.id.itemGroupIcon);
            }

            public void setGroup(ExpandableGroup group) {
                Group g = ((Group) group);
                genreName.setText(group.getTitle());
                icon.setBackgroundResource(g.getIconResId());

                for (int i = 0; i < expandedGroupIds.size(); i++) {
                    int id = expandedGroupIds.get(i);
                    if (id == g.getId()) {
                        animateExpand();
                        return;
                    }
                }
                animateCollapse();
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
                animationUtils.rotateExpand(arrow);
            }

            private void animateCollapse() {
                animationUtils.rotateCollapse(arrow);
            }
        }


        public GroupsAdapter(List<Group> groups) {
            super(groups);
        }

        @Override
        public CustomGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_group, parent, false);
            return new CustomGroupViewHolder(view);
        }


        @Override
        public void onBindGroupViewHolder(CustomGroupViewHolder holder, int flatPosition,
                                          ExpandableGroup group) {
            holder.setGroup(group);
        }

        @Override
        public GroupItemViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_group_item, parent, false);
            return new GroupItemViewHolder(view);
        }

        @Override
        public void onBindCheckChildViewHolder(GroupItemViewHolder holder, int flatPosition,
                                               CheckedExpandableGroup group, int childIndex) {
            final GroupItem groupItem = (GroupItem) group.getItems().get(childIndex);
            holder.setGroupItemName(groupItem.getName());
        }
    }


}
