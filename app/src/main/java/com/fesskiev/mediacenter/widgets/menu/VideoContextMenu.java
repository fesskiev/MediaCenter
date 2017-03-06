package com.fesskiev.mediacenter.widgets.menu;


import android.content.Context;
import android.view.LayoutInflater;

import com.fesskiev.mediacenter.R;

public class VideoContextMenu extends ContextMenu {

    public interface OnVideoContextMenuListener {

        void onAddVideoToPlayList();

        void onDeleteVideo();

        void onDetailsVideoFile();
    }

    private OnVideoContextMenuListener listener;

    public VideoContextMenu(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.video_context_menu_layout, this, true);

        findViewById(R.id.menuDetailsVideoFile).setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsVideoFile();
            }
            ContextMenuManager.getInstance().hideContextMenu();
        });

        findViewById(R.id.menuAddVideoToPlayList).setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddVideoToPlayList();
            }
            ContextMenuManager.getInstance().hideContextMenu();
        });
        findViewById(R.id.menuDeleteVideo).setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteVideo();
            }
            ContextMenuManager.getInstance().hideContextMenu();
        });
        findViewById(R.id.menuCancel).setOnClickListener(v -> ContextMenuManager.getInstance().hideContextMenu());
    }

    public void setOnVideoContextMenuListener(OnVideoContextMenuListener l) {
        this.listener = l;
    }
}
