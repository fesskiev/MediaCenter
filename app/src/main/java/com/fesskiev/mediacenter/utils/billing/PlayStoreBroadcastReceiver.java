package com.fesskiev.mediacenter.utils.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Receives "com.android.vending.billing.PURCHASES_UPDATED" from the Play Store and notifies the
 * listener.
 */
class PlayStoreBroadcastReceiver extends BroadcastReceiver {

    private static final String ACTION = "com.android.vending.billing.PURCHASES_UPDATED";

    private Context context;
    private OnPlayStoreListener listener;

    public PlayStoreBroadcastReceiver(Context context) {
        this.context = context;
    }

    void setOnPlayStoreListener(OnPlayStoreListener listener) {
        this.listener = listener;
    }

    void removeOnPlayStoreListener() {
        listener = null;
        context.unregisterReceiver(this);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !TextUtils.equals(intent.getAction(), ACTION)) {
            return;
        }
        if (listener != null) {
            listener.onPurchasesChanged();
        }
    }

}
