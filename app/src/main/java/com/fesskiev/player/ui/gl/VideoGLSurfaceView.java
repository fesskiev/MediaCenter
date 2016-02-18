package com.fesskiev.player.ui.gl;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class VideoGLSurfaceView extends GLSurfaceView {

    private VideoRenderer renderer;

    public VideoGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public VideoGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        setEGLContextClientVersion(2);
        renderer = new VideoRenderer();
        setRenderer(renderer);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public SurfaceTexture getSurfaceTexture() {
        return renderer.getSurfaceTexture();
    }

}

