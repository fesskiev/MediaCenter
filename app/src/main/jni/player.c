#include <jni.h>
#include <string.h>
#include <assert.h>
#include <SLES/OpenSLES.h>
#include<android/log.h>


// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;
static SLEqualizerItf eqOutputItf = NULL;

// URI player interfaces
static SLObjectItf uriPlayerObject = NULL;
static SLPlayItf uriPlayerPlay;
static SLSeekItf uriPlayerSeek;
static SLMuteSoloItf uriPlayerMuteSolo;
static SLVolumeItf uriPlayerVolume;

static JavaVM *gJavaVM;
static jobject gCallbackObject = NULL;

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native", "JNI_OnLoad");
    gJavaVM = vm;
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_registerCallback(JNIEnv *env, jobject instance) {
    __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native", "register callback");
    gCallbackObject = (*env)->NewGlobalRef(env, instance);
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_unregisterCallback(JNIEnv *env,
                                                                     jobject instance) {
    __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native", "unregister callback");
    (*env)->DeleteGlobalRef(env, gCallbackObject);
    gCallbackObject = NULL;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_createEngine(JNIEnv *env, jobject instance) {

    SLresult result;

    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    const SLInterfaceID ids[1] = {SL_IID_EQUALIZER};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;


    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_EQUALIZER,
                                              &eqOutputItf);

    assert(SL_RESULT_SUCCESS == result);
    (void) result;


}


void callbackCalled(int event) {
    int status;
    JNIEnv *env;
    int isAttached = 0;

    if (!gCallbackObject) return;

    if ((status = (*gJavaVM)->GetEnv(gJavaVM, (void**)&env, JNI_VERSION_1_6)) < 0) {
        if ((status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &env, NULL)) < 0) {
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
    __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native", "The value is %d", event);
    callbackCalled(event);

}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_createUriAudioPlayer(JNIEnv *env,
                                                                       jobject instance,
                                                                       jstring uri) {
    SLresult result;

    // convert Java string to UTF-8
    const char *utf8 = (*env)->GetStringUTFChars(env, uri, NULL);
    assert(NULL != utf8);

    // configure audio source
    // (requires the INTERNET permission depending on the uri parameter)
    SLDataLocator_URI loc_uri = {SL_DATALOCATOR_URI, (SLchar *) utf8};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource audioSrc = {&loc_uri, &format_mime};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_SEEK, SL_IID_MUTESOLO, SL_IID_VOLUME};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &uriPlayerObject, &audioSrc,
                                                &audioSnk, 3, ids, req);
    // note that an invalid URI is not detected here, but during prepare/prefetch on Android,
    // or possibly during Realize on other platforms
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

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
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the seek interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_SEEK, &uriPlayerSeek);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the mute/solo interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_MUTESOLO, &uriPlayerMuteSolo);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // get the volume interface
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_VOLUME, &uriPlayerVolume);
    assert(SL_RESULT_SUCCESS == result);
    (void) result;

    // register callback function
    result = (*uriPlayerPlay)->RegisterCallback(uriPlayerPlay,
                                                playStatusCallback, 0);
    assert(SL_RESULT_SUCCESS == result);
    result = (*uriPlayerPlay)->SetCallbackEventsMask(uriPlayerPlay,
                                                     SL_PLAYEVENT_HEADATEND);
    assert(SL_RESULT_SUCCESS == result);

    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setPlayingUriAudioPlayer(JNIEnv *env,
                                                                           jobject instance,
                                                                           jboolean isPlaying) {

    SLresult result;

    // make sure the URI audio player was created
    if (NULL != uriPlayerPlay) {

        // set the player's state
        result = (*uriPlayerPlay)->SetPlayState(uriPlayerPlay, isPlaying ?
                                                               SL_PLAYSTATE_PLAYING
                                                                         : SL_PLAYSTATE_PAUSED);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }


}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setVolumeUriAudioPlayer(JNIEnv *env,
                                                                          jobject instance,
                                                                          jint milliBel) {

    SLresult result;
    if (NULL != uriPlayerVolume) {
        result = (*uriPlayerVolume)->SetVolumeLevel(uriPlayerVolume, milliBel);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setSeekUriAudioPlayer(JNIEnv *env,
                                                                        jobject instance,
                                                                        jlong milliseconds) {
    SLresult result;

    if (NULL != uriPlayerSeek) {

        result = (*uriPlayerSeek)->SetPosition(uriPlayerSeek, milliseconds, SL_SEEKMODE_FAST);

        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_releaseUriAudioPlayer(JNIEnv *env,
                                                                        jobject instance) {

    // destroy URI audio player object, and invalidate all associated interfaces
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

        assert(SL_RESULT_SUCCESS == result);
        return msec;
    }

    return 0;


}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getPosition(JNIEnv *env, jobject instance) {

    if (NULL != uriPlayerPlay) {

        SLmillisecond msec;
        SLresult result = (*uriPlayerPlay)->GetPosition(uriPlayerPlay, &msec);
        assert(SL_RESULT_SUCCESS == result);
        return msec;
    }

    return 0;

}

SLuint32 getPlayState() {
    SLresult result;


    if (NULL != uriPlayerPlay) {

        SLuint32 state;
        result = (*uriPlayerPlay)->GetPlayState(uriPlayerPlay, &state);
        assert(SL_RESULT_SUCCESS == result);
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

    SLresult result;
    if (NULL != eqOutputItf) {

        result = (*eqOutputItf)->SetEnabled(eqOutputItf, isEnable ?
                                                         SL_BOOLEAN_TRUE : SL_BOOLEAN_FALSE);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }


}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_usePreset(JNIEnv *env, jobject instance,
                                                            jint presetValue) {

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->UsePreset(eqOutputItf, (SLuint16) presetValue);
        assert(SL_RESULT_SUCCESS == result);
    }

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getNumberOfBands(JNIEnv *env, jobject instance) {

    SLuint16 numberBands = 0;

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfBands(eqOutputItf, &numberBands);
        assert(SL_RESULT_SUCCESS == result);
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
        assert(SL_RESULT_SUCCESS == result);
    }

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getBandLevel(JNIEnv *env, jobject instance,
                                                               jint bandNumber) {
    SLmillibel level;
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
        assert(SL_RESULT_SUCCESS == result);
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
        assert(SL_RESULT_SUCCESS == result);
    }

    return (*env)->NewStringUTF(env, namePreset);
}

