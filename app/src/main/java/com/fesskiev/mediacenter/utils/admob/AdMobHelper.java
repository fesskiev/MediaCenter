package com.fesskiev.mediacenter.utils.admob;


import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.Map;

public class AdMobHelper {

    public static final int KEY_AUDIO_BANNER = 0;
    public static final int KEY_VIDEO_BANNER = 1;

    private static AdMobHelper INSTANCE;

    private Map<RelativeLayout, AdView> adViewArrayMap;
    private Context context;

    public static AdMobHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdMobHelper();
        }
        return INSTANCE;
    }

    private AdMobHelper() {
        adViewArrayMap = new ArrayMap<>();
        context = MediaApplication.getInstance().getApplicationContext();
    }

    public void createAdView(RelativeLayout layout, int bannerId) {
        RelativeLayout.LayoutParams adsParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        adsParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        AdView adview = new AdView(context);
        adview.setAdSize(AdSize.BANNER);

        switch (bannerId) {
            case KEY_VIDEO_BANNER:
                adview.setAdUnitId(context.getResources().getString(R.string.banner_ad_unit_id));
                break;
            case KEY_AUDIO_BANNER:
                adview.setAdUnitId(context.getResources().getString(R.string.banner_ad_unit_id_1));
                break;
        }
        final AdListener listener = new AdListener() {
            @Override
            public void onAdLoaded() {
                adview.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        };
        adview.setAdListener(listener);
        adview.setVisibility(View.GONE);

        layout.addView(adview, adsParams);

        requestAd(adview);

        adViewArrayMap.put(layout, adview);
    }

    private void requestAd(AdView adview) {
        AdRequest adRequest = new AdRequest.Builder().build();
        adview.loadAd(adRequest);
    }

    public void resumeAdMob() {
        if (!adViewArrayMap.isEmpty()) {
            for (Map.Entry<RelativeLayout, AdView> entry : adViewArrayMap.entrySet()) {
                AdView adView = entry.getValue();
                adView.resume();
                Log.w("admob", "resume admob");
            }
        }
    }

    public void pauseAdMob() {
        if (!adViewArrayMap.isEmpty()) {
            for (Map.Entry<RelativeLayout, AdView> entry : adViewArrayMap.entrySet()) {
                AdView adView = entry.getValue();
                adView.pause();
                Log.w("admob", "pause admob");
            }
        }
    }

    public void destroyAdView() {
        for (Map.Entry<RelativeLayout, AdView> entry : adViewArrayMap.entrySet()) {
            AdView adView = entry.getValue();
            adView.destroy();

            RelativeLayout relativeLayout = entry.getKey();
            relativeLayout.removeAllViews();
            relativeLayout.setVisibility(View.GONE);
            Log.w("admob", "destroy admod");
        }
        adViewArrayMap.clear();
    }
}
