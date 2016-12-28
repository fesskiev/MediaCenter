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
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;


public class MediaNavigationView extends NavigationView {

    public interface OnEQStateChangedListener {

        void onStateChanged(boolean enable);
    }

    public interface OnEQClickListener {

        void onEQClick();
    }

    private OnEQStateChangedListener eqStateChangedListener;
    private OnEQClickListener eqClickListener;

    private EffectsAdapter adapter;
    private boolean enableEQ;

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
            if (eqClickListener != null) {
                eqClickListener.onEQClick();
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

        public class EQViewHolder extends RecyclerView.ViewHolder {

            SwitchCompat eqStateSwitch;

            public EQViewHolder(View v) {
                super(v);
                eqStateSwitch = (SwitchCompat) v.findViewById(R.id.eqSwitch);
                eqStateSwitch.setOnClickListener(view -> {
                    boolean checked = ((SwitchCompat) view).isChecked();
                    if (eqStateChangedListener != null) {
                        eqStateChangedListener.onStateChanged(checked);
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
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case VIEW_TYPE_EQ:
                    createEqItem((EQViewHolder) holder);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        private void createEqItem(EQViewHolder holder) {
            holder.eqStateSwitch.setChecked(enableEQ);
        }

    }

    public void setEqStateChangedListener(OnEQStateChangedListener l) {
        this.eqStateChangedListener = l;
    }

    public void setEQClickListener(OnEQClickListener l) {
        this.eqClickListener = l;
    }

    public void setEQEnable(boolean enable) {
        this.enableEQ = enable;
        adapter.notifyDataSetChanged();
    }
}
