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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fesskiev.player.R;
import com.fesskiev.player.model.VKMusicFile;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.http.URLHelper;
import com.fesskiev.player.widgets.MaterialProgressBar;
import com.fesskiev.player.widgets.recycleview.OnItemClickListener;
import com.fesskiev.player.widgets.recycleview.RecycleItemClickListener;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;

import java.util.ArrayList;
import java.util.List;


public class MusicVKFragment extends Fragment {

    public static MusicVKFragment newInstance() {
        return new MusicVKFragment();
    }

    private AudioAdapter audioAdapter;
    private MaterialProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_vk, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        audioAdapter = new AudioAdapter();
        recyclerView.setAdapter(audioAdapter);
        recyclerView.addOnItemTouchListener(new RecycleItemClickListener(getActivity(),
                new OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {
                    }
                }));

        progressBar = (MaterialProgressBar) view.findViewById(R.id.progressBar);
        showProgressBar();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void fetchUserAudio() {
        AppSettingsManager manager = new AppSettingsManager(getActivity());
        RESTService.fetchAudio(getActivity(),
                URLHelper.getAudioURL(manager.getAuthToken(), manager.getUserId(), 20, 0));
    }

    private void registerBroadcastReceiver() {
        Log.d("test", "registerBroadcastReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(RESTService.ACTION_AUDIO_RESULT);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(audioReceiver,
                filter);
    }

    private void unregisterBroadcastReceiver() {
        Log.d("test", "unregisterBroadcastReceiver");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(audioReceiver);
    }

    private BroadcastReceiver audioReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RESTService.ACTION_AUDIO_RESULT:
                    Log.d("test", "RESTService.ACTION_AUDIO_RESULT");
                    ArrayList<VKMusicFile> vkMusicFiles =
                            intent.getParcelableArrayListExtra(RESTService.EXTRA_AUDIO_RESULT);
                    if (vkMusicFiles != null) {
                        Log.d("test", "refresh");
                        hideProgressBar();
                        audioAdapter.refresh(vkMusicFiles);
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

    private class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.ViewHolder> {

        private List<VKMusicFile> notices;

        public AudioAdapter() {
            this.notices = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView artist;
            TextView title;

            public ViewHolder(View v) {
                super(v);

                artist = (TextView) v.findViewById(R.id.itemArtist);
                title = (TextView) v.findViewById(R.id.itemTitle);

            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_audio, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            VKMusicFile vkMusicFile = notices.get(position);
            if (vkMusicFile != null) {

                holder.artist.setText(String.valueOf(vkMusicFile.artist));
                holder.title.setText(String.valueOf(vkMusicFile.title));
            }

        }

        public void refresh(List<VKMusicFile> notices) {
            this.notices.clear();
            this.notices.addAll(notices);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return notices.size();
        }
    }
}
