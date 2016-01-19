package com.fesskiev.player.ui.vk;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.model.vk.Group;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.http.URLHelper;
import com.fesskiev.player.widgets.recycleview.OnItemClickListener;
import com.fesskiev.player.widgets.recycleview.RecycleItemClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class GroupsFragment extends Fragment {


    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }

    private GroupsAdapter groupsAdapter;
    private List<Group> groups;
    private AppSettingsManager appSettingsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();

        appSettingsManager = new AppSettingsManager(getActivity());
        RESTService.fetchGroups(getActivity(), URLHelper.getUserGroupsURL(appSettingsManager.getAuthToken(),
                appSettingsManager.getUserId()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        groupsAdapter = new GroupsAdapter();
        recyclerView.setAdapter(groupsAdapter);
        recyclerView.addOnItemTouchListener(new RecycleItemClickListener(getActivity(),
                new OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                        Group group = groups.get(position);
                        if(group != null){
                            RESTService.fetchGroupAudio(getActivity(),
                                    URLHelper.getGroupAudioURL(appSettingsManager.getAuthToken(),
                                            group.gid, 20, 0));
                        }
                    }
                }));
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RESTService.ACTION_GROUPS_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(groupReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(groupReceiver);
    }


    private BroadcastReceiver groupReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RESTService.ACTION_GROUPS_RESULT:
                    groups = intent.getParcelableArrayListExtra(RESTService.EXTRA_GROUPS_RESULT);
                    if (groups != null) {
                        groupsAdapter.refresh(groups);
                    }
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
    }

    private class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

        private List<Group> groups;

        public GroupsAdapter() {
            this.groups = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView groupName;
            TextView screenName;
            ImageView coverGroup;
            TextView groupType;

            public ViewHolder(View v) {
                super(v);

                groupName = (TextView) v.findViewById(R.id.groupName);
                screenName = (TextView) v.findViewById(R.id.screenName);
                coverGroup = (ImageView) v.findViewById(R.id.coverGroup);
                groupType = (TextView) v.findViewById(R.id.groupType);

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Group group = groups.get(position);
            if (group != null) {

                holder.groupName.setText(group.name);
                holder.screenName.setText(group.screenName);
                holder.groupType.setText(group.type);

                Picasso.with(getActivity()).
                        load(group.photoMediumURL).
                        resize(256, 256).
                        into(holder.coverGroup);
            }
        }

        public void refresh(List<Group> newGroups) {
            this.groups.clear();
            this.groups.addAll(newGroups);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }
    }

}
