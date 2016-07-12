package com.fesskiev.player.widgets.dialogs.effects;

import android.content.Context;
import android.os.Bundle;

import com.fesskiev.player.R;
import com.fesskiev.player.services.PlaybackService;
import com.fesskiev.player.utils.AppSettingsManager;

public class BassBoostDialog extends EffectDialog {

    public static void getInstance(Context context) {
        BassBoostDialog dialog = new BassBoostDialog(context);
        dialog.show();
    }

    private AppSettingsManager settingsManager;

    protected BassBoostDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsManager = AppSettingsManager.getInstance(getContext());

        setEffectName(getContext().getString(R.string.drawer_item_bass));
        setEffectIcon(R.drawable.icon_bass_boost);
        setBassBoostValue();
    }

    private void setBassBoostValue() {
        if (settingsManager != null) {
            int value = settingsManager.getBassBoostValue();
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
        settingsManager.setBassBoostValue(value);
        PlaybackService.changeBassBoostLevel(getContext());
    }
}
