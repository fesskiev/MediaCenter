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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.model.vk.Group;
import com.fesskiev.player.model.vk.GroupPost;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.utils.download.DownloadGroupAudioFile;
import com.fesskiev.player.utils.http.URLHelper;
import com.fesskiev.player.widgets.MaterialProgressBar;
import com.fesskiev.player.widgets.cards.GroupPostCardView;
import com.fesskiev.player.widgets.recycleview.EndlessScrollListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;


public class GroupAudioFragment extends Fragment {

    private static final String GROUP_BUNDLE = "com.fesskiev.player.GROUP_BUNDLE";

    private Group group;
    private GroupPostsAdapter adapter;
    private MaterialProgressBar progressBar;
    protected int postsOffset;

    public static GroupAudioFragment newInstance(Group group) {
        GroupAudioFragment groupAudioFragment = new GroupAudioFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(GROUP_BUNDLE, group);
        groupAudioFragment.setArguments(bundle);
        return groupAudioFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            group = getArguments().getParcelable(GROUP_BUNDLE);
        }
        registerBroadcastReceiver();
        postsOffset = 20;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_audio, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        ScrollingLinearLayoutManager layoutManager = new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GroupPostsAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new EndlessScrollListener(layoutManager) {
            @Override
            public void onEndlessScrolled() {
                postsOffset += 20;
                fetchPosts();
            }
        });

        fetchPosts();
    }

    private void fetchPosts() {
        RESTService.fetchGroupPost(getActivity(), URLHelper.getGroupPostURL(group.gid, postsOffset, 0));
        showProgressBar();
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(RESTService.ACTION_GROUP_POSTS_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(groupAudioReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(groupAudioReceiver);
    }

    private BroadcastReceiver groupAudioReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RESTService.ACTION_GROUP_POSTS_RESULT:
                    ArrayList<GroupPost> groupPosts =
                            intent.getParcelableArrayListExtra(RESTService.EXTRA_GROUP_POSTS);
                    if (groupPosts != null) {
                        getGroupDownloadAudioFiles(groupPosts);
                        adapter.refresh(groupPosts);
                        hideProgressBar();
                    }
                    break;
            }
        }
    };

    private void getGroupDownloadAudioFiles(ArrayList<GroupPost> groupPosts){
        for(GroupPost groupPost : groupPosts){
            groupPost.downloadGroupAudioFiles =
                    DownloadGroupAudioFile.getDownloadGroupAudioFiles(getActivity(), groupPost.musicFiles);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceiver();
    }

    private class GroupPostsAdapter extends RecyclerView.Adapter<GroupPostsAdapter.ViewHolder> {

        private ArrayList<GroupPost> groupPosts;

        public GroupPostsAdapter() {
            this.groupPosts = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            GroupPostCardView groupPostCardView;



            public ViewHolder(View v) {
                super(v);

                groupPostCardView = (GroupPostCardView)v.findViewById(R.id.groupPostView);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_post, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            GroupPost groupPost = groupPosts.get(position);
            if(groupPost != null){
                holder.groupPostCardView.setGroupPost(groupPost);
            }
        }

        @Override
        public int getItemCount() {
            return groupPosts.size();
        }

        public void refresh(ArrayList<GroupPost> groupPosts) {
            this.groupPosts.addAll(groupPosts);
            notifyDataSetChanged();
        }
    }
}
