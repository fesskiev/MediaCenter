package com.fesskiev.mediacenter.vk;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.vk.Audio;
import com.fesskiev.mediacenter.data.model.vk.Group;
import com.fesskiev.mediacenter.data.model.vk.GroupPost;

import com.fesskiev.mediacenter.data.source.remote.ErrorHelper;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.utils.download.DownloadGroupAudioFile;
import com.fesskiev.mediacenter.widgets.GroupPostAudioView;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;
import com.fesskiev.mediacenter.widgets.recycleview.EndlessScrollListener;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class GroupAudioFragment extends Fragment {

    private static final String GROUP_BUNDLE = "com.fesskiev.player.GROUP_BUNDLE";

    private Subscription subscription;
    private Group group;
    private GroupPostsAdapter adapter;
    private MaterialProgressBar progressBar;
    private int postsOffset;

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
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
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
        showProgressBar();
        subscription = MediaApplication.getInstance().getRepository().getGroupPots(group.getId(), postsOffset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(groupPostsResponse -> {
                    updateGroups(groupPostsResponse.getGroupPostList());
                }, this::checkRequestError);
    }

    private void updateGroups(List<GroupPost> groupPostList) {
        if (groupPostList != null) {
            getGroupDownloadAudioFiles(groupPostList);
            adapter.refresh(groupPostList);
            hideProgressBar();
        }
    }

    private void checkRequestError(Throwable throwable) {
        ErrorHelper.getInstance().createErrorSnackBar(getActivity(), throwable,
                new ErrorHelper.OnErrorHandlerListener() {
            @Override
            public void tryRequestAgain() {
                fetchPosts();
            }

            @Override
            public void show(Snackbar snackbar) {

            }

            @Override
            public void hide(Snackbar snackbar) {

            }
        });
        hideProgressBar();
    }

    private void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }


    private void getGroupDownloadAudioFiles(List<GroupPost> groupPosts) {
        for (GroupPost groupPost : groupPosts) {
            List<Audio> audios = groupPost.getAudio();
            if (audios != null) {
                groupPost.setDownloadGroupAudioFiles(
                        DownloadGroupAudioFile.getDownloadGroupAudioFiles(getActivity(), audios));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }

    private class GroupPostsAdapter extends RecyclerView.Adapter<GroupPostsAdapter.ViewHolder> {

        private ArrayList<GroupPost> groupPosts;

        public GroupPostsAdapter() {
            this.groupPosts = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView postText;
            ImageView postCover;
            TextView likes;
            TextView shares;
            TextView time;
            ImageView openCloseButton;
            GroupPostAudioView audioCardView;

            public ViewHolder(View v) {
                super(v);

                postText = (TextView) v.findViewById(R.id.postText);
                postCover = (ImageView) v.findViewById(R.id.postCover);
                likes = (TextView) v.findViewById(R.id.likePost);
                shares = (TextView) v.findViewById(R.id.sharePost);
                time = (TextView) v.findViewById(R.id.timeText);
                openCloseButton = (ImageView) v.findViewById(R.id.openCloseButton);
                openCloseButton.setOnClickListener(this);
                audioCardView = (GroupPostAudioView) v.findViewById(R.id.groupPostAudioView);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.openCloseButton:
                        GroupPost groupPost = groupPosts.get(getAdapterPosition());
                        if (groupPost != null) {
                            groupPost.setOpenMusicItems(!groupPost.isOpenMusicItems());
                            adapter.notifyItemChanged(getAdapterPosition());
                        }
                        break;
                }
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
            if (groupPost != null) {

                holder.postText.setText(Html.fromHtml(groupPost.getText()));
                holder.likes.setText(String.valueOf(groupPost.getLikes().getCount()));
                holder.shares.setText(String.valueOf(groupPost.getReposts().getCount()));
                holder.time.setText(Utils.getDateStringFromSeconds(groupPost.getDate()));

                if (groupPost.getAttachments().get(0).getPhoto() != null) {
                    holder.postCover.setVisibility(View.VISIBLE);

                    BitmapHelper.getInstance().loadURIBitmap(groupPost.getAttachments().get(0).getPhoto().getPhoto604(),
                            holder.postCover);
                } else {
                    holder.postCover.setVisibility(View.GONE);
                }

                if (!groupPost.getDownloadGroupAudioFiles().isEmpty()) {
                    holder.openCloseButton.setVisibility(View.VISIBLE);

                } else {
                    holder.openCloseButton.setVisibility(View.GONE);
                }

                if (groupPost.isOpenMusicItems()) {
                    holder.openCloseButton.setImageResource(R.drawable.icon_up);
                    holder.audioCardView.createAudioItems(groupPost.getDownloadGroupAudioFiles());
                } else {
                    holder.openCloseButton.setImageResource(R.drawable.icon_down);
                    holder.audioCardView.removeAudioItems();
                }

            }
        }

        @Override
        public int getItemCount() {
            return groupPosts.size();
        }

        public void refresh(List<GroupPost> groupPosts) {
            this.groupPosts.addAll(groupPosts);
            notifyDataSetChanged();
        }
    }
}
