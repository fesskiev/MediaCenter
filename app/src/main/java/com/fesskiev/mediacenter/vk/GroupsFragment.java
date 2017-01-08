package com.fesskiev.mediacenter.vk;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.model.vk.Group;
import com.fesskiev.mediacenter.data.source.remote.ErrorHelper;
import com.fesskiev.mediacenter.utils.BitmapHelper;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;
import com.fesskiev.mediacenter.widgets.recycleview.RecyclerItemTouchClickListener;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class GroupsFragment extends Fragment {

    public static GroupsFragment newInstance() {
        return new GroupsFragment();
    }

    public static final String GROUP_EXTRA = "com.fesskiev.player.GROUP_EXTRA";

    private Subscription subscription;
    private GroupsAdapter groupsAdapter;
    private MaterialProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_groups, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        groupsAdapter = new GroupsAdapter();
        recyclerView.setAdapter(groupsAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemTouchClickListener(getActivity(),
                new RecyclerItemTouchClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View childView, int position) {
                        Group group = groupsAdapter.getGroups().get(position);
                        if (group != null) {
                            startGroupAudioActivity(group);
                        }
                    }

                    @Override
                    public void onItemLongPress(View childView, int position) {

                    }
                }));

        fetchGroups();
    }


    public void fetchGroups() {
        showProgressBar();
        subscription = MediaApplication.getInstance().getRepository().getGroups()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(groupsResponse -> {
                    updateGroups(groupsResponse.getGroups().getGroupsList());
                }, this::checkRequestError);

    }

    private void updateGroups(List<Group> groupsList) {
        groupsAdapter.refresh(groupsList);
        hideProgressBar();
    }

    private void checkRequestError(Throwable throwable) {
        ErrorHelper.getInstance().createErrorSnackBar(getActivity(), throwable,
                new ErrorHelper.OnErrorHandlerListener() {
                    @Override
                    public void tryRequestAgain() {
                        fetchGroups();
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

    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }


    private void startGroupAudioActivity(Group group) {
        Intent intent = new Intent(getActivity(), GroupAudioActivity.class);
        intent.putExtra(GROUP_EXTRA, group);
        startActivity(intent);
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

                holder.groupName.setText(group.getName());
                holder.screenName.setText(group.getScreenName());
                holder.groupType.setText(group.getType());

                BitmapHelper.getInstance().loadCircleURIBitmap(group.getPhotoMediumURL(), holder.coverGroup);

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

        public List<Group> getGroups() {
            return groups;
        }
    }

}
