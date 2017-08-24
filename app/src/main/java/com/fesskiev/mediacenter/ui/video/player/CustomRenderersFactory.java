package com.fesskiev.mediacenter.ui.video.player;

import android.content.Context;
import android.os.Handler;

import com.fesskiev.extensions.player.audio.FfmpegAudioRenderer;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.text.TextRenderer;
import com.google.android.exoplayer2.video.MediaCodecVideoRenderer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;


import java.lang.reflect.Constructor;
import java.util.ArrayList;

class CustomRenderersFactory implements RenderersFactory {

    private final Context context;

    CustomRenderersFactory(Context context) {
        this.context = context;
    }

    @Override
    public Renderer[] createRenderers(Handler eventHandler, VideoRendererEventListener videoRendererEventListener,
                                      AudioRendererEventListener audioRendererEventListener,
                                      TextRenderer.Output textRendererOutput, MetadataRenderer.Output metadataRendererOutput) {

        ArrayList<Renderer> out = new ArrayList<>();

        out.add(new FfmpegAudioRenderer(
                eventHandler,
                audioRendererEventListener)
        );

        out.add(new MediaCodecVideoRenderer(context,
                MediaCodecSelector.DEFAULT,
                5000,
                null,
                false,
                eventHandler,
                videoRendererEventListener,
                50));

        try {
            Class<?> clazz =
                    Class.forName("com.google.android.exoplayer2.ext.vp9.LibvpxVideoRenderer");
            Constructor<?> constructor = clazz.getConstructor(boolean.class, long.class, Handler.class,
                    VideoRendererEventListener.class, int.class);
            Renderer renderer = (Renderer) constructor.newInstance(true, 5000,
                    eventHandler, videoRendererEventListener, 50);
            out.add(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Class<?> clazz =
                    Class.forName("com.google.android.exoplayer2.ext.flac.LibflacAudioRenderer");
            Constructor<?> constructor = clazz.getConstructor(Handler.class,
                    AudioRendererEventListener.class, AudioProcessor[].class);
            Renderer renderer = (Renderer) constructor.newInstance(eventHandler, audioRendererEventListener,
                    new AudioProcessor[0]);
            out.add(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }


        out.add(new TextRenderer(textRendererOutput, eventHandler.getLooper()));
        out.add(new MetadataRenderer(metadataRendererOutput, eventHandler.getLooper()));

        return out.toArray(new Renderer[out.size()]);
    }
}
