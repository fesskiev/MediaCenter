package com.fesskiev.player.widgets.dialogs.effects;

import android.content.Context;
import android.os.Bundle;

import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.AppSettingsManager;

public class VirtualizerDialog extends EffectDialog {

    public static void getInstance(Context context) {
        VirtualizerDialog dialog = new VirtualizerDialog(context);
        dialog.show();
    }

    private AppSettingsManager settingsManager;

    public VirtualizerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsManager = AppSettingsManager.getInstance(getContext());

        setEffectName(getContext().getString(R.string.drawer_item_virtualizer));
        setEffectIcon(R.drawable.icon_virtualizer);
        setVirtualizerValue();
    }


    private void setVirtualizerValue() {
        if (settingsManager != null) {
            int value = settingsManager.getVirtualizerValue();
            if (value != -1) {
                setProgress(value);
                setProgressText(value);
            } else {
                setProgress(0);
                setProgressText(0);
            }
        }
    }

    @Override
    public void getEffectValue(int value) {
        settingsManager.setVirtualizerValue(value);
        PlaybackService.changeVirtualizerLevel(getContext());
    }
}
