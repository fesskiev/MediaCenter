package com.fesskiev.mediacenter.widgets.menu;


import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;

import com.fesskiev.mediacenter.R;

public class FolderContextMenu extends ContextMenu{

    public interface OnFolderContextMenuListener {

        void onDeleteFolder();

        void onSearchData();

        void onDetailsFolder();
    }

    private OnFolderContextMenuListener listener;

    public FolderContextMenu(Context context, boolean needSearch) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.folder_context_menu_layout, this, true);

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


        Button searchDataButton = findViewById(R.id.menuSearchData);
        if(needSearch) {
            searchDataButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSearchData();
                }
                ContextMenuManager.getInstance().hideContextMenu();
            });
        } else {
            searchDataButton.setVisibility(GONE);
        }


        findViewById(R.id.menuCancel).setOnClickListener(v -> ContextMenuManager.getInstance().hideContextMenu());
    }

    public void setOnFolderContextMenuListener(OnFolderContextMenuListener l) {
        this.listener = l;
    }
}
