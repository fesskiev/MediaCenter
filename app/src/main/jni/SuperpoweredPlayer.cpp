#include "SuperpoweredPlayer.h"
#include "Superpowered/SuperpoweredSimple.h"
#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>

static SuperpoweredPlayer *player = NULL;

static void playerEventCallback(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event,
                                void *__unused value) {

    SuperpoweredAdvancedAudioPlayer *player = *((SuperpoweredAdvancedAudioPlayer **) clientData);
    switch (event) {
        case SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess:
            player->setBpm(126.0f);
            player->setFirstBeatMs(353);
            player->setPosition(player->firstBeatMs, false, false);
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_LoadError:
            __android_log_print(ANDROID_LOG_DEBUG, "HLSExample", "Open error: %s", (char *) value);
            break;
        case SuperpoweredAdvancedAudioPlayerEvent_EOF:
            __android_log_print(ANDROID_LOG_DEBUG, "HLSExample", "END SONG");
            break;
        default:;
    };
}

static bool audioProcessing(void *clientdata, short int *audioIO, int numberOfSamples,
                            int __unused samplerate) {
    return ((SuperpoweredPlayer *) clientdata)->process(audioIO, (unsigned int) numberOfSamples);
}

SuperpoweredPlayer::SuperpoweredPlayer(unsigned int samplerate, unsigned int buffersize,
                                       const char *path) : volume(1.0f) {

    buffer = (float *) memalign(16, (buffersize + 16) * sizeof(float) * 2);

    player = new SuperpoweredAdvancedAudioPlayer(&player, playerEventCallback, samplerate, 0);
    player->open(path);

    player->syncMode = SuperpoweredAdvancedAudioPlayerSyncMode_TempoAndBeat;

    audioSystem = new SuperpoweredAndroidAudioIO(samplerate, buffersize, false, true,
                                                 audioProcessing, this, -1, SL_ANDROID_STREAM_MEDIA,
                                                 buffersize * 2);
}

bool SuperpoweredPlayer::process(short int *output, unsigned int numberOfSamples) {

    if (player->process(buffer, false, numberOfSamples, volume)) {
        SuperpoweredFloatToShortInt(buffer, output, numberOfSamples);
        return true;
    } else return false;
}

void SuperpoweredPlayer::setPlaying(bool isPlaying) {
    if (isPlaying) {
        player->play(false);
    } else {
        player->pause();
    }
}


SuperpoweredPlayer::~SuperpoweredPlayer() {
    delete audioSystem;
    delete player;
    free(buffer);
}

void SuperpoweredPlayer::setVolume(float value) {
    volume = value * 0.01f;
}

void SuperpoweredPlayer::setSeek(int value) {
    player->seek(value * 0.01);
}

int SuperpoweredPlayer::getDuration() {
    return player->durationMs;
}

int SuperpoweredPlayer::getPosition() {
    return (int) player->positionMs;
}

bool SuperpoweredPlayer::isPlaying() {
    return player->playing;
}

void SuperpoweredPlayer::setLooping(bool looping) {

}

void SuperpoweredPlayer::open(const char *path) {
    player->open(path);
}


extern "C" JNIEXPORT void
Java_com_fesskiev_player_services_PlaybackService_setLoopingAudioPlayer(JNIEnv *env, jclass type,
                                                                        jboolean isLooping) {
    player->setLooping(isLooping);

}

extern "C" JNIEXPORT jboolean
Java_com_fesskiev_player_services_PlaybackService_isPlaying(JNIEnv *env, jobject instance) {
    return player->isPlaying();
}

extern "C" JNIEXPORT jint
Java_com_fesskiev_player_services_PlaybackService_getPosition(JNIEnv *env, jobject instance) {

    return player->getPosition();

}

extern "C" JNIEXPORT jint
Java_com_fesskiev_player_services_PlaybackService_getDuration(JNIEnv *env, jobject instance) {

    return player->getDuration();

}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_services_PlaybackService_createAudioPlayer(JNIEnv *env, jobject instance,
                                                                    jstring path, jint sampleRate,
                                                                    jint bufferSize) {
    const char *str = env->GetStringUTFChars(path, 0);

    player = new SuperpoweredPlayer((unsigned int) sampleRate, (unsigned int) bufferSize, str);

    env->ReleaseStringUTFChars(path, str);
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_services_PlaybackService_openAudioFile(JNIEnv *env, jobject instance,
                                                                    jstring path) {
    const char *str = env->GetStringUTFChars(path, 0);

    if(player != nullptr){
        player->open(str);
    }


    env->ReleaseStringUTFChars(path, str);
}


extern "C" JNIEXPORT void
Java_com_fesskiev_player_services_PlaybackService_setPlayingAudioPlayer(JNIEnv *env,
                                                                        jobject instance,
                                                                        jboolean isPlaying) {
    player->setPlaying(isPlaying);
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_services_PlaybackService_setVolumeAudioPlayer(JNIEnv *env,
                                                                       jobject instance,
                                                                       jint value) {
    player->setVolume(value);

}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_services_PlaybackService_setSeekAudioPlayer(JNIEnv *env, jobject instance,
                                                                     jint value) {
    player->setSeek(value);
}