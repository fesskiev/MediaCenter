package com.fesskiev.player.ui.vk;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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

import java.util.List;

public class MusicVKActivity extends AppCompatActivity {

    private static final String TAG = MusicVKActivity.class.getName();
    private AppSettingsManager settingsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_vk);
        settingsManager = AppSettingsManager.getInstance(this);

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

                toolbar.inflateMenu(R.menu.vk_menu);
                toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.vk_settings:
                                break;
                            case R.id.vk_show_download_folder:
                                break;
                        }
                        return true;
                    }
                });
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content, MusicVKFragment.newInstance(),
                    MusicVKFragment.class.getName());
            transaction.commit();

        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (settingsManager.isAuthTokenEmpty()) {
                    String[] vkScope = new String[]{VKScope.DIRECT, VKScope.AUDIO};
                    VKSdk.login(MusicVKActivity.this, vkScope);
                } else {
                    makeRequestMusicFiles();
                }
            }
        }, 1000);

    }

    private void makeRequestMusicFiles() {
        MusicVKFragment musicVKFragment = (MusicVKFragment) getSupportFragmentManager().
                findFragmentByTag(MusicVKFragment.class.getName());
        if (musicVKFragment != null) {
            List<Fragment> registeredFragments = musicVKFragment.getRegisteredFragments();
            if (registeredFragments != null) {
                for (Fragment fragment : registeredFragments) {
                    if (fragment instanceof RecyclerAudioFragment) {
                        ((RecyclerAudioFragment) fragment).fetchAudio(0);
                    }
                }
            }
        }
    }

    private void makeRequestUserProfile() {
        RESTService.fetchUserProfile(this, URLHelper.getUserProfileURL(settingsManager.getUserId()));
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
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
