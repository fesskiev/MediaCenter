package com.fesskiev.player.widgets.nav;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.fesskiev.player.R;
import com.fesskiev.player.widgets.recycleview.ScrollingLinearLayoutManager;


public class MediaNavigationView extends NavigationView {

    public interface OnEQStateChangedListener {

        void onStateChanged(boolean enable);
    }

    private OnEQStateChangedListener eqStateChangedListener;

    private ImageView headerAnimation;

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

        headerAnimation = (ImageView) view.findViewById(R.id.effectHeaderAnimation);

        RecyclerView effectsList = (RecyclerView) view.findViewById(R.id.effectsList);
        if (effectsList != null) {
            effectsList.setLayoutManager(new ScrollingLinearLayoutManager(context,
                    LinearLayoutManager.VERTICAL, false, 1000));
            effectsList.setAdapter(new EffectsAdapter());
        }
    }


    private class EffectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_EQ = 0;


        public class EQViewHolder extends RecyclerView.ViewHolder {

            SwitchCompat eqStateSwitch;

            public EQViewHolder(View v) {
                super(v);
                eqStateSwitch = (SwitchCompat) v.findViewById(R.id.eqSwitch);
                eqStateSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                    if (eqStateChangedListener != null) {
                        eqStateChangedListener.onStateChanged(isChecked);
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

        }

    }

    public void setEqStateChangedListener(OnEQStateChangedListener l) {
        this.eqStateChangedListener = l;
    }

    public void startHeaderAnimation() {
        ((AnimationDrawable) headerAnimation.getDrawable()).start();
    }

    public void stopHeaderAnimation() {
        ((AnimationDrawable) headerAnimation.getDrawable()).stop();
    }
}
