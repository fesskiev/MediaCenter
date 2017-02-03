package com.fesskiev.mediacenter.widgets.nav;

import android.content.Context;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.widgets.fetch.FetchContentView;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;


public class MediaNavigationView extends NavigationView {

    public interface OnEffectChangedListener {

        void onEffectClick();

        void onEQStateChanged(boolean enable);

        void onReverbStateChanged(boolean enable);

        void onWhooshStateChanged(boolean enable);

        void onEchoStateChanged(boolean enable);
    }

    private OnEffectChangedListener listener;

    private EffectsAdapter adapter;
    private FetchContentView fetchContentView;
    private boolean enableEQ;
    private boolean enableReverb;
    private boolean enableWhoosh;
    private boolean enableEcho;


    public MediaNavigationView(Context context) {
        super(context);
        init(context);
    }


    public MediaNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MediaNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.nav_effects_layout, this, true);

        view.findViewById(R.id.imageHeader).setOnClickListener(v -> {
            if (listener != null) {
                listener.onEffectClick();
            }
        });

        RecyclerView effectsList = (RecyclerView) view.findViewById(R.id.effectsList);
        if (effectsList != null) {
            effectsList.setLayoutManager(new ScrollingLinearLayoutManager(context,
                    LinearLayoutManager.VERTICAL, false, 1000));
            adapter = new EffectsAdapter();
            effectsList.setAdapter(adapter);
        }
    }


    private class EffectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_EQ = 0;
        private static final int VIEW_TYPE_REVERB = 1;
        private static final int VIEW_TYPE_ECHO = 2;
        private static final int VIEW_TYPE_WHOOSH = 3;
        private static final int VIEW_TYPE_FETCH_CONTENT = 4;


        public class EchoViewHolder extends RecyclerView.ViewHolder {

            SwitchCompat echoStateSwitch;

            public EchoViewHolder(View v) {
                super(v);
                echoStateSwitch = (SwitchCompat) v.findViewById(R.id.echoSwitch);
                echoStateSwitch.setOnClickListener(view -> {
                    boolean checked = ((SwitchCompat) view).isChecked();
                    if (listener != null) {
                        listener.onEchoStateChanged(checked);
                    }
                });
            }
        }

        public class FetchContentHolder extends RecyclerView.ViewHolder {

            public FetchContentHolder(View v) {
                super(v);
                fetchContentView = (FetchContentView) v.findViewById(R.id.fetchContentView);
            }
        }


        public class WhooshViewHolder extends RecyclerView.ViewHolder {

            SwitchCompat whooshStateSwitch;

            public WhooshViewHolder(View v) {
                super(v);
                whooshStateSwitch = (SwitchCompat) v.findViewById(R.id.whooshSwitch);
                whooshStateSwitch.setOnClickListener(view -> {
                    boolean checked = ((SwitchCompat) view).isChecked();
                    if (listener != null) {
                        listener.onWhooshStateChanged(checked);
                    }
                });
            }
        }

        public class EQViewHolder extends RecyclerView.ViewHolder {

            SwitchCompat eqStateSwitch;

            public EQViewHolder(View v) {
                super(v);
                eqStateSwitch = (SwitchCompat) v.findViewById(R.id.eqSwitch);
                eqStateSwitch.setOnClickListener(view -> {
                    boolean checked = ((SwitchCompat) view).isChecked();
                    if (listener != null) {
                        listener.onEQStateChanged(checked);
                    }
                });
            }
        }

        public class ReverbViewHolder extends RecyclerView.ViewHolder {

            SwitchCompat reverbStateSwitch;

            public ReverbViewHolder(View v) {
                super(v);
                reverbStateSwitch = (SwitchCompat) v.findViewById(R.id.reverbSwitch);
                reverbStateSwitch.setOnClickListener(view -> {
                    boolean checked = ((SwitchCompat) view).isChecked();
                    if (listener != null) {
                        listener.onReverbStateChanged(checked);
                    }
                });
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            switch (viewType) {
                case VIEW_TYPE_EQ:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.nav_drawer_eq_layout, parent, false);
                    return new EQViewHolder(v);
                case VIEW_TYPE_REVERB:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.nav_drawer_reverb_layout, parent, false);
                    return new ReverbViewHolder(v);
                case VIEW_TYPE_ECHO:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.nav_drawer_echo_layout, parent, false);
                    return new EchoViewHolder(v);
                case VIEW_TYPE_WHOOSH:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.nav_drawer_whoosh_layout, parent, false);
                    return new WhooshViewHolder(v);
                case VIEW_TYPE_FETCH_CONTENT:
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.nav_drawer_fetch_content, parent, false);
                    return new FetchContentHolder(v);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_EQ:
                    createEqItem((EQViewHolder) holder);
                    break;
                case VIEW_TYPE_REVERB:
                    createReverbItem((ReverbViewHolder) holder);
                    break;
                case VIEW_TYPE_ECHO:
                    createEchoItem((EchoViewHolder) holder);
                    break;
                case VIEW_TYPE_WHOOSH:
                    createWhooshItem((WhooshViewHolder) holder);
                    break;
                case VIEW_TYPE_FETCH_CONTENT:
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                    return VIEW_TYPE_EQ;
                case 1:
                    return VIEW_TYPE_REVERB;
                case 2:
                    return VIEW_TYPE_ECHO;
                case 3:
                    return VIEW_TYPE_WHOOSH;
                case 4:
                    return VIEW_TYPE_FETCH_CONTENT;
            }
            return -1;
        }

        @Override
        public int getItemCount() {
            return 5;
        }

        private void createEchoItem(EchoViewHolder holder) {
            holder.echoStateSwitch.setChecked(enableEcho);
        }

        private void createEqItem(EQViewHolder holder) {
            holder.eqStateSwitch.setChecked(enableEQ);
        }

        private void createReverbItem(ReverbViewHolder holder) {
            holder.reverbStateSwitch.setChecked(enableReverb);
        }

        private void createWhooshItem(EffectsAdapter.WhooshViewHolder holder) {
            holder.whooshStateSwitch.setChecked(enableWhoosh);
        }
    }


    public void setOnEffectChangedListener(OnEffectChangedListener l) {
        this.listener = l;
    }

    public void setEQEnable(boolean enable) {
        this.enableEQ = enable;
        adapter.notifyDataSetChanged();
    }

    public void setReverbEnable(boolean enable) {
        this.enableReverb = enable;
        adapter.notifyDataSetChanged();
    }

    public void setWhooshEnable(boolean enable) {
        this.enableWhoosh = enable;
        adapter.notifyDataSetChanged();
    }

    public void setEchoEnable(boolean enable) {
        this.enableEcho = enable;
        adapter.notifyDataSetChanged();
    }

    public FetchContentView getFetchContentView() {
        return fetchContentView;
    }
}
