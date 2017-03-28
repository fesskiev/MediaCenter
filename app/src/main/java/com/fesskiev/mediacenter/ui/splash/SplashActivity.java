package com.fesskiev.mediacenter.ui.splash;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.ui.walkthrough.WalkthroughFragment;
import com.fesskiev.mediacenter.utils.AppSettingsManager;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppSettingsManager settingsManager = AppSettingsManager.getInstance();
        if (settingsManager.isFirstStartApp()) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, WalkthroughFragment.newInstance(),
                    WalkthroughFragment.class.getName());
            transaction.commit();

        } else {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, SplashFragment.newInstance(),
                    SplashFragment.class.getName());
            transaction.commit();

        }
    }
}
