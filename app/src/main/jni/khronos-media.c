#include <jni.h>
#include <string.h>
#include <assert.h>
#include <SLES/OpenSLES.h>
#include<android/log.h>

// engine interfaces
SLObjectItf engineObject = NULL;
SLEngineItf engineEngine;

// output mix interfaces
SLObjectItf outputMixObject = NULL;
SLEqualizerItf eqOutputItf = NULL;

// URI player interfaces
SLObjectItf uriPlayerObject = NULL;
SLPlayItf uriPlayerPlay;
SLSeekItf uriPlayerSeek;
SLMuteSoloItf uriPlayerMuteSolo;
SLVolumeItf uriPlayerVolume;
SLBassBoostItf uriBassBoost;

JavaVM *gJavaVM;
jobject gCallbackObject = NULL;

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native", "JNI_OnLoad");
    gJavaVM = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_registerCallback(JNIEnv *env, jobject instance) {
    gCallbackObject = (*env)->NewGlobalRef(env, instance);
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_unregisterCallback(JNIEnv *env,
                                                                     jobject instance) {
    (*env)->DeleteGlobalRef(env, gCallbackObject);
    gCallbackObject = NULL;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_createEngine(JNIEnv *env, jobject instance) {

    SLresult result;

    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);

    const SLInterfaceID ids[2] = {SL_IID_EQUALIZER, SL_IID_BASSBOOST};
    const SLboolean req[2] = {SL_BOOLEAN_FALSE, SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 2, ids, req);

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);

    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_EQUALIZER,
                                              &eqOutputItf);

    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_BASSBOOST,
                                              &uriBassBoost);
}


void handlingCallback(int event) {
    JNIEnv *env;
    int isAttached = 0;

    if (!gCallbackObject) return;

    if (((*gJavaVM)->GetEnv(gJavaVM, (void **) &env, JNI_VERSION_1_6)) < 0) {
        if (((*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL)) < 0) {
            return;
        }
        isAttached = 1;
    }

    jclass cls = (*env)->GetObjectClass(env, gCallbackObject);
    if (!cls) {
        if (isAttached) (*gJavaVM)->DetachCurrentThread(gJavaVM);
        return;
    }


    jmethodID method = (*env)->GetMethodID(env, cls, "playStatusCallback", "(I)V");
    if (!method) {
        if (isAttached) (*gJavaVM)->DetachCurrentThread(gJavaVM);
        return;
    }

    (*env)->CallVoidMethod(env, gCallbackObject, method, event);

    if (isAttached) (*gJavaVM)->DetachCurrentThread(gJavaVM);
}


void playStatusCallback(SLPlayItf play, void *context, SLuint32 event) {
//    __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native", "The value is %d", event);
    handlingCallback(event);
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_createUriAudioPlayer(JNIEnv *env,
                                                                       jobject instance,
                                                                       jstring uri) {
    SLresult result;

    // convert Java string to UTF-8
    const char *utf8 = (*env)->GetStringUTFChars(env, uri, NULL);

    // configure audio source
    // (requires the INTERNET permission depending on the uri parameter)
    SLDataLocator_URI loc_uri = {SL_DATALOCATOR_URI, (SLchar *) utf8};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource audioSrc = {&loc_uri, &format_mime};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID ids[4] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME, SL_IID_BASSBOOST};
    const SLboolean req[4] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &uriPlayerObject, &audioSrc,
                                                &audioSnk, 4, ids, req);
    // release the Java string and UTF-8
    (*env)->ReleaseStringUTFChars(env, uri, utf8);

    // realize the player
    result = (*uriPlayerObject)->Realize(uriPlayerObject, SL_BOOLEAN_FALSE);
    // this will always succeed on Android, but we check result for portability to other platforms
    if (SL_RESULT_SUCCESS != result) {
        (*uriPlayerObject)->Destroy(uriPlayerObject);
        uriPlayerObject = NULL;
        return JNI_FALSE;
    }

    // get the play interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_PLAY, &uriPlayerPlay);

    // get the seek interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_SEEK, &uriPlayerSeek);

    // get the mute/solo interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_MUTESOLO, &uriPlayerMuteSolo);

    // get the volume interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_VOLUME, &uriPlayerVolume);


    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_BASSBOOST,
                                              (void *) &uriBassBoost);

    // register callback function
    result = (*uriPlayerPlay)->RegisterCallback(uriPlayerPlay,
                                                playStatusCallback, 0);
    result = (*uriPlayerPlay)->SetCallbackEventsMask(uriPlayerPlay,
                                                     SL_PLAYEVENT_HEADATEND);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setPlayingUriAudioPlayer(JNIEnv *env,
                                                                           jobject instance,
                                                                           jboolean isPlaying) {
    if (NULL != uriPlayerPlay) {
        SLresult result = (*uriPlayerPlay)->SetPlayState(uriPlayerPlay, isPlaying ?
                                                                        SL_PLAYSTATE_PLAYING : SL_PLAYSTATE_PAUSED);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setVolumeUriAudioPlayer(JNIEnv *env,
                                                                          jobject instance,
                                                                          jint milliBel) {
    if (NULL != uriPlayerVolume) {
        SLresult result = (*uriPlayerVolume)->SetVolumeLevel(uriPlayerVolume, milliBel);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setSeekUriAudioPlayer(JNIEnv *env,
                                                                        jobject instance,
                                                                        jlong milliseconds) {
    if (NULL != uriPlayerSeek) {
        SLresult result = (*uriPlayerSeek)->SetPosition(uriPlayerSeek, milliseconds,
                                                        SL_SEEKMODE_FAST);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_releaseUriAudioPlayer(JNIEnv *env,
                                                                        jobject instance) {
    if (uriPlayerObject != NULL) {
        (*uriPlayerObject)->Destroy(uriPlayerObject);
        uriPlayerObject = NULL;
        uriPlayerPlay = NULL;
        uriPlayerSeek = NULL;
        uriPlayerMuteSolo = NULL;
        uriPlayerVolume = NULL;
    }

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_releaseEngine(JNIEnv *env, jobject instance) {

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        eqOutputItf = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != NULL) {
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getDuration(JNIEnv *env, jobject instance) {

    if (NULL != uriPlayerPlay) {

        SLmillisecond msec;
        SLresult result = (*uriPlayerPlay)->GetDuration(uriPlayerPlay, &msec);
        return msec;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getPosition(JNIEnv *env, jobject instance) {

    if (NULL != uriPlayerPlay) {

        SLmillisecond msec;
        SLresult result = (*uriPlayerPlay)->GetPosition(uriPlayerPlay, &msec);
        return msec;
    }

    return 0;
}

SLuint32 getPlayState() {

    if (NULL != uriPlayerPlay) {

        SLuint32 state;
        SLresult result = (*uriPlayerPlay)->GetPlayState(uriPlayerPlay, &state);
        (void) result;

        return state;
    }
    return 0;
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_isPlaying(JNIEnv *env, jobject instance) {

    return getPlayState() == SL_PLAYSTATE_PLAYING;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setEnableEQ(JNIEnv *env, jobject instance,
                                                              jboolean isEnable) {
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->SetEnabled(eqOutputItf, isEnable ?
                                                         SL_BOOLEAN_TRUE : SL_BOOLEAN_FALSE);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_usePreset(JNIEnv *env, jobject instance,
                                                            jint presetValue) {

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->UsePreset(eqOutputItf, (SLuint16) presetValue);
    }
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getNumberOfBands(JNIEnv *env, jobject instance) {

    SLuint16 numberBands = 0;

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfBands(eqOutputItf, &numberBands);
    }
    return numberBands;
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getNumberOfPresets(JNIEnv *env,
                                                                     jobject instance) {

    SLuint16 numberPresets = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfPresets(eqOutputItf, &numberPresets);
        assert(SL_RESULT_SUCCESS == result);
    }
    return numberPresets;

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getCurrentPreset(JNIEnv *env, jobject instance) {

    SLuint16 preset = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetCurrentPreset(eqOutputItf, &preset);
        assert(SL_RESULT_SUCCESS == result);
    }
    return preset;

}

JNIEXPORT jintArray JNICALL
Java_com_fesskiev_player_services_PlaybackService_getBandLevelRange(JNIEnv *env, jobject instance) {

    int size = 2;
    jintArray result;
    result = (*env)->NewIntArray(env, size);
    if (result == NULL) {
        return NULL;
    }

    SLmillibel minLevel, maxLevel = 0;
    jint fill[size];
    if (NULL != eqOutputItf) {
        (*eqOutputItf)->GetBandLevelRange(eqOutputItf, &minLevel, &maxLevel);

        fill[0] = minLevel;
        fill[1] = maxLevel;

        __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                            "Band level range = %dmB to %dmB\n", minLevel, maxLevel);
    }

    (*env)->SetIntArrayRegion(env, result, 0, size, fill);
    return result;

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setBandLevel(JNIEnv *env, jobject instance,
                                                               jint bandNumber, jint milliBel) {
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->SetBandLevel(eqOutputItf, (SLuint16) bandNumber,
                                                       (SLmillibel) milliBel);
    }
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getBandLevel(JNIEnv *env, jobject instance,
                                                               jint bandNumber) {
    SLmillibel level = NULL;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetBandLevel(eqOutputItf, (SLuint16) bandNumber, &level);
        assert(SL_RESULT_SUCCESS == result);
    }
    return level;

}

JNIEXPORT jintArray JNICALL
Java_com_fesskiev_player_services_PlaybackService_getBandFrequencyRange(JNIEnv *env,
                                                                        jobject instance,
                                                                        jint bandNumber) {
    int size = 2;
    jintArray result;
    result = (*env)->NewIntArray(env, size);
    if (result == NULL) {
        return NULL;
    }

    SLmilliHertz minFreg;
    SLmilliHertz maxFreg;
    jint fill[size];

    if (NULL != eqOutputItf) {
        (*eqOutputItf)->GetBandFreqRange(eqOutputItf,
                                         (SLuint16) bandNumber, &minFreg, &maxFreg);

        __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                            "Band %d freg = %dHz to %dHz\n", bandNumber, minFreg / 1000,
                            maxFreg / 1000);

        fill[0] = minFreg;
        fill[1] = maxFreg;
    }

    (*env)->SetIntArrayRegion(env, result, 0, size, fill);
    return result;

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getCenterFrequency(JNIEnv *env, jobject instance,
                                                                     jint bandNumber) {
    SLmilliHertz centerFreq;
    if (NULL != eqOutputItf) {
        SLresult result =
                (*eqOutputItf)->GetCenterFreq(eqOutputItf, (SLuint16) bandNumber, &centerFreq);
        assert(SL_RESULT_SUCCESS == result);
    }
    return centerFreq;

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getNumberOfPreset(JNIEnv *env, jobject instance) {

    SLuint16 numberPreset = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfPresets(eqOutputItf, &numberPreset);
    }
    return numberPreset;

}

JNIEXPORT jstring JNICALL
Java_com_fesskiev_player_services_PlaybackService_getPresetName(JNIEnv *env, jobject instance,
                                                                jint presetNumber) {
    const SLchar *namePreset = NULL;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetPresetName(eqOutputItf, (SLuint16) presetNumber,
                                                        &namePreset);
    }
    return (*env)->NewStringUTF(env, namePreset);

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setEnableBassBoost(JNIEnv *env, jobject instance,
                                                                     jboolean isEnable) {
    if (NULL != uriBassBoost) {

        SLboolean enable = isEnable ?
                           SL_BOOLEAN_TRUE : SL_BOOLEAN_FALSE;
        SLresult result = (*uriBassBoost)->SetEnabled(uriBassBoost, enable);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_isSupportedBassBoost(JNIEnv *env,
                                                                       jobject instance) {
    if (NULL != uriBassBoost) {
        SLboolean strengthSupported = SL_BOOLEAN_FALSE;
        SLresult result = (*uriBassBoost)->IsStrengthSupported(uriBassBoost, &strengthSupported);
        return (jboolean) strengthSupported;
    }
    return JNI_FALSE;
}


JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setBassBoostValue(JNIEnv *env, jobject instance,
                                                                    jint value) {
    if (NULL != uriBassBoost) {
        SLresult result = (*uriBassBoost)->SetStrength(uriBassBoost, (SLuint16) value);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_isEnabledBassBoost(JNIEnv *env,
                                                                     jobject instance) {
    if (NULL != uriBassBoost) {
        SLboolean strengthSupported = SL_BOOLEAN_FALSE;
        SLresult result = (*uriBassBoost)->IsEnabled(uriBassBoost, &strengthSupported);
        return (jboolean) strengthSupported;
    }

    return JNI_FALSE;
}