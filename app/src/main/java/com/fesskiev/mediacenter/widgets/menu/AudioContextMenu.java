package com.fesskiev.mediacenter.widgets.menu;


import android.content.Context;
import android.view.LayoutInflater;

import com.fesskiev.mediacenter.R;

public class AudioContextMenu extends ContextMenu{

    public interface OnAudioContextMenuListener {

        void onDeleteAudioFolder();
    }

    private OnAudioContextMenuListener listener;

    public AudioContextMenu(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.audio_context_menu_layout, this, true);

        findViewById(R.id.menuDeleteAudioFolder).setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteAudioFolder();
            }
            ContextMenuManager.getInstance().hideContextMenu();
        });
        findViewById(R.id.menuCancel).setOnClickListener(v -> ContextMenuManager.getInstance().hideContextMenu());
    }

    public void setOnAudioContextMenuListener(OnAudioContextMenuListener l) {
        this.listener = l;
    }
}
