package com.fesskiev.player.ui.player;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Surface;

import com.fesskiev.player.MediaApplication;
import com.fesskiev.player.R;
import com.fesskiev.player.model.VideoFile;
import com.fesskiev.player.ui.gl.VideoGLSurfaceView;

public class VideoPlayerActivity extends AppCompatActivity implements Playable {


    public static native boolean createStreamingMediaPlayer(String filename);

    public static native void setPlayingStreamingMediaPlayer(boolean isPlaying);

    public static native void shutdown();

    public static native void setSurface(Surface surface);

    public static native void rewindStreamingMediaPlayer();


    private VideoGLSurfaceView glSurfaceView;
    private VideoFile videoFile;

    private static final String TAG = VideoPlayerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        videoFile = MediaApplication.getInstance().getVideoPlayer().currentVideoFile;
        glSurfaceView = (VideoGLSurfaceView) findViewById(R.id.videoGLSurfaceView);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    @Override
    public void createPlayer() {

    }

    @Override
    public void play() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void next() {

    }

    @Override
    public void previous() {

    }
}
