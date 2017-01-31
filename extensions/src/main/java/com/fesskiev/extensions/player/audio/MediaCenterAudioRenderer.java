package com.fesskiev.extensions.player.audio;

import android.os.Handler;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.util.MimeTypes;

public class MediaCenterAudioRenderer extends FfmpegAudioRenderer {

    private boolean ac3Passthrough;

    public MediaCenterAudioRenderer(Handler eventHandler, AudioRendererEventListener eventListener, boolean ac3Passthrough) {
        super(eventHandler, eventListener);
        this.ac3Passthrough = ac3Passthrough;
    }

    @Override
    public int supportsFormat(Format format) {
        if(ac3Passthrough && MimeTypes.isAudio(format.sampleMimeType)) {
            if(format.sampleMimeType.equals(MimeTypes.AUDIO_E_AC3)||
                    format.sampleMimeType.equals(MimeTypes.AUDIO_AC3)) {
                return FORMAT_UNSUPPORTED_SUBTYPE;
            }
        }

        return super.supportsFormat(format);
    }
}
