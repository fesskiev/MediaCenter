#include "SuperpoweredPlayer.h"
#include "Superpowered/SuperpoweredSimple.h"
#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>

static SuperpoweredPlayer *player = NULL;
JavaVM *gJavaVM;
jobject gCallbackObject = NULL;

static void handlingCallback(int event) {
    JNIEnv *env;
    int isAttached = 0;

    if (!gCallbackObject) {
        return;
    }

    if ((gJavaVM->GetEnv((void **) &env, JNI_VERSION_1_6)) < 0) {
        if ((gJavaVM->AttachCurrentThread(&env, NULL)) < 0) {
            return;
        }
        isAttached = 1;
    }

    jclass cls = env->GetObjectClass(gCallbackObject);
    if (!cls) {
        if (isAttached) {
            gJavaVM->DetachCurrentThread();
        }
        return;
    }

    jmethodID method = env->GetMethodID(cls, "playStatusCallback", "(I)V");
    if (!method) {
        if (isAttached) {
            gJavaVM->DetachCurrentThread();
        }
        return;
    }

    env->CallVoidMethod(gCallbackObject, method, event);

    if (isAttached) {
        gJavaVM->DetachCurrentThread();
    }
}

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
            handlingCallback(1);
            break;
        default:;
    };
}

static bool audioProcessing(void *clientdata, short int *audioIO, int numberOfSamples,
                            int __unused samplerate) {
//    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredExample", "audioProcessing");

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

    mixer = new SuperpoweredStereoMixer();
    bandEQ = new Superpowered3BandEQ(samplerate);
}

bool SuperpoweredPlayer::process(short int *output, unsigned int numberOfSamples) {

    bool silence = !player->process(buffer, false, numberOfSamples, volume);

    bandEQ->process(buffer, buffer, numberOfSamples);

    if (!silence) {
        SuperpoweredFloatToShortInt(buffer, output, numberOfSamples);
    }

    return !silence;
}

void SuperpoweredPlayer::setPlaying(bool isPlaying) {
    if (isPlaying) {
        player->play(false);
    } else {
        player->pause();
    }
}


SuperpoweredPlayer::~SuperpoweredPlayer() {
    delete bandEQ;
    delete mixer;
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
    player->looping = looping;
}

void SuperpoweredPlayer::open(const char *path) {
    player->open(path);
}

void SuperpoweredPlayer::setEQBands(int index, int value) {
    if (index < 0 || index > 2) {
        return;
    }
    float bandF = 1.0f;
    if (value < 50) {
        bandF = (float) value / 50.0f;
    } else if (value > 50) {
        bandF = 1.0f + (float) (value - 50) / 10.0f;
    }
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredExample",
                        "setEQBands index = %i bandF = %f",
                        index, bandF);
    bandEQ->bands[index] = bandF;
}

void SuperpoweredPlayer::enableEQ(bool enable) {
    bandEQ->enable(enable);
}

void SuperpoweredPlayer::onBackground() {
    audioSystem->onBackground();
}

void SuperpoweredPlayer::onForeground() {
    audioSystem->onForeground();
}



JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    gJavaVM = vm;
    return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_onDestroy(JNIEnv *env, jobject instance) {
    if (player != nullptr) {
        player->~SuperpoweredPlayer();
        __android_log_print(ANDROID_LOG_DEBUG, "HLSExample", "DESTROY@@@@!!!!");
    }
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_onBackground(JNIEnv *env, jobject instance) {
    if (player != nullptr) {
        player->onBackground();
    }
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_onForeground(JNIEnv *env, jobject instance) {
    if (player != nullptr) {
        player->onForeground();
    }

}


extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_registerCallback(JNIEnv *env, jobject instance) {
    gCallbackObject = env->NewGlobalRef(instance);
}


extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_unregisterCallback(JNIEnv *env,
                                                                   jobject instance) {
    env->DeleteGlobalRef(gCallbackObject);
    gCallbackObject = NULL;
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_enableEQ(JNIEnv *env, jobject instance,
                                                         jboolean enable) {
    player->enableEQ(enable);
}



extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_setEQBands(JNIEnv *javaEnvironment, jobject obj,
                                                           jint band, jint value) {
    player->setEQBands(band, value);
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_setLoopingAudioPlayer(JNIEnv *env, jobject instance,
                                                                      jboolean isLooping) {
    player->setLooping(isLooping);

}

extern "C" JNIEXPORT jboolean
Java_com_fesskiev_player_SuperPoweredSDKWrapper_isPlaying(JNIEnv *env, jobject instance) {
    return player->isPlaying();
}

extern "C" JNIEXPORT jint
Java_com_fesskiev_player_SuperPoweredSDKWrapper_getPosition(JNIEnv *env, jobject instance) {

    return player->getPosition();

}

extern "C" JNIEXPORT jint
Java_com_fesskiev_player_SuperPoweredSDKWrapper_getDuration(JNIEnv *env, jobject instance) {

    return player->getDuration();

}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_createAudioPlayer(JNIEnv *env, jobject instance,
                                                                  jstring path, jint sampleRate,
                                                                  jint bufferSize) {

    const char *str = env->GetStringUTFChars(path, 0);

    player = new SuperpoweredPlayer((unsigned int) sampleRate, (unsigned int) bufferSize, str);

    env->ReleaseStringUTFChars(path, str);
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_openAudioFile(JNIEnv *env, jobject instance,
                                                              jstring path) {
    const char *str = env->GetStringUTFChars(path, 0);

    if (player != nullptr) {
        player->open(str);
    }


    env->ReleaseStringUTFChars(path, str);
}


extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_setPlayingAudioPlayer(JNIEnv *env,
                                                                      jobject instance,
                                                                      jboolean isPlaying) {
    player->setPlaying(isPlaying);
}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_setVolumeAudioPlayer(JNIEnv *env,
                                                                     jobject instance,
                                                                     jint value) {
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredExample", "setVolume = %i", value);
    player->setVolume(value);

}

extern "C" JNIEXPORT void
Java_com_fesskiev_player_SuperPoweredSDKWrapper_setSeekAudioPlayer(JNIEnv *env, jobject instance,
                                                                   jint value) {
    player->setSeek(value);
}