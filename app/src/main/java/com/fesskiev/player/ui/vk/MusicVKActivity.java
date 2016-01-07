package com.fesskiev.player.ui.vk;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.fesskiev.player.R;
import com.fesskiev.player.services.RESTService;
import com.fesskiev.player.ui.MainActivity;
import com.fesskiev.player.utils.AppSettingsManager;
import com.fesskiev.player.utils.http.URLHelper;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class MusicVKActivity extends AppCompatActivity {

    private static final String TAG = MusicVKActivity.class.getName();
    private AppSettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_vk);
        settingsManager =
                new AppSettingsManager(getApplicationContext());

        if (savedInstanceState == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle(getString(R.string.title_music_vk_activity));
                toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        navigateUpToFromChild(MusicVKActivity.this,
                                IntentCompat.makeMainActivity(new ComponentName(MusicVKActivity.this,
                                        MainActivity.class)));
                    }
                });
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, MusicVKFragment.newInstance(),
                    MusicVKFragment.class.getName());
            transaction.commit();

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(settingsManager.isAuthTokenEmpty()){
            String[] vkScope = new String[]{VKScope.DIRECT, VKScope.AUDIO};
            VKSdk.login(this, vkScope);
        } else {
            makeRequestMusicFiles();
            makeRequestUserProfile();
        }
    }

    private void makeRequestUserProfile(){
        AppSettingsManager manager = new AppSettingsManager(this);
        RESTService.fetchUserProfile(this, URLHelper.getUserProfileURL(manager.getUserId()));
    }

    private void makeRequestMusicFiles(){
        MusicVKFragment musicVKFragment = (MusicVKFragment)getSupportFragmentManager().
                findFragmentByTag(MusicVKFragment.class.getName());
        if(musicVKFragment != null) {
            musicVKFragment.fetchUserAudio();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Log.d(TAG, "auth success: " + res.accessToken);

                settingsManager.setAuthToken(res.accessToken);
                settingsManager.setAuthSecret(res.secret);
                settingsManager.setUserId(res.userId);

                makeRequestMusicFiles();
                makeRequestUserProfile();

            }

            @Override
            public void onError(VKError error) {
                Log.d(TAG, "auth fail: " + error.errorMessage);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
