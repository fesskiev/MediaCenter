package com.fesskiev.mediacenter.widgets.menu;


import android.content.Context;
import android.view.LayoutInflater;

import com.fesskiev.mediacenter.R;

public class FolderContextMenu extends ContextMenu{

    public interface OnFolderContextMenuListener {

        void onDeleteFolder();

        void onDetailsFolder();
    }

    private OnFolderContextMenuListener listener;

    public FolderContextMenu(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.audio_context_menu_layout, this, true);

        findViewById(R.id.menuDetailsFolder).setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsFolder();
            }
            ContextMenuManager.getInstance().hideContextMenu();
        });

        findViewById(R.id.menuDeleteFolder).setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteFolder();
            }
            ContextMenuManager.getInstance().hideContextMenu();
        });
        findViewById(R.id.menuCancel).setOnClickListener(v -> ContextMenuManager.getInstance().hideContextMenu());
    }

    public void setOnFolderContextMenuListener(OnFolderContextMenuListener l) {
        this.listener = l;
    }
}
