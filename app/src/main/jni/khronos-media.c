#include <jni.h>
#include <stdio.h>
#include <fcntl.h>
#include <SLES/OpenSLES.h>
#include<android/log.h>

#define LOG_FORMAT(x, y)  __android_log_print(ANDROID_LOG_VERBOSE, "OpenSL ES", x, y)
#define LOG(x)  __android_log_print(ANDROID_LOG_VERBOSE, "OpenSL ES", x)

SLObjectItf engineObject = NULL;
SLEngineItf engineEngine = NULL;

SLObjectItf outputMixObject = NULL;
SLEqualizerItf eqOutputItf = NULL;

SLObjectItf uriPlayerObject = NULL;
SLPlayItf uriPlayerPlay = NULL;
SLSeekItf uriPlayerSeek = NULL;
SLMuteSoloItf uriPlayerMuteSolo = NULL;
SLVolumeItf uriPlayerVolume = NULL;
SLBassBoostItf uriBassBoost = NULL;
SLVirtualizerItf uriVirtualizer = NULL;
SLPrefetchStatusItf uriPrefetchItf = NULL;


JavaVM *gJavaVM;
jobject gCallbackObject = NULL;


void checkError(SLresult res) {
    if (res != SL_RESULT_SUCCESS) {
        LOG_FORMAT("%u SL failure\n", res);
    }
    else {
//        LOG_FORMAT("%d SL success, proceeding...\n", res);
    }
}


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

    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    checkError(result);

    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    checkError(result);

    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    checkError(result);

    const SLInterfaceID ids[1] = {SL_IID_VIRTUALIZER};

    const SLboolean req[1] = {SL_BOOLEAN_TRUE};

    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    checkError(result);

    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    checkError(result);

    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_VIRTUALIZER, &uriVirtualizer);
    checkError(result);


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
//    LOG("The value is %d", event);
    handlingCallback(event);
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_createUriAudioPlayer(JNIEnv *env,
                                                                       jobject instance,
                                                                       jstring uri) {
    SLresult result;

    SLDataSource audioSource;
    SLDataLocator_URI locatorUri;
    SLDataFormat_MIME mime;

    SLDataSink audioSink;
    SLDataLocator_OutputMix locatorOutputMix;

    const char *utf8 = (*env)->GetStringUTFChars(env, uri, NULL);

    locatorOutputMix.locatorType = SL_DATALOCATOR_OUTPUTMIX;
    locatorOutputMix.outputMix = outputMixObject;
    audioSink.pLocator = (void *) &locatorOutputMix;
    audioSink.pFormat = NULL;

    locatorUri.locatorType = SL_DATALOCATOR_URI;
    locatorUri.URI = (SLchar *) utf8;
    mime.formatType = SL_DATAFORMAT_MIME;
    mime.mimeType = (SLchar *) NULL;
    mime.containerType = SL_CONTAINERTYPE_UNSPECIFIED;

    audioSource.pFormat = (void *) &mime;
    audioSource.pLocator = (void *) &locatorUri;

    const SLInterfaceID ids[7] = {SL_IID_PLAY,
                                  SL_IID_SEEK,
                                  SL_IID_MUTESOLO,
                                  SL_IID_VOLUME,
                                  SL_IID_BASSBOOST,
                                  SL_IID_EQUALIZER,
                                  SL_IID_PREFETCHSTATUS};

    const SLboolean req[7] = {SL_BOOLEAN_TRUE,
                              SL_BOOLEAN_TRUE,
                              SL_BOOLEAN_TRUE,
                              SL_BOOLEAN_TRUE,
                              SL_BOOLEAN_TRUE,
                              SL_BOOLEAN_TRUE,
                              SL_BOOLEAN_TRUE};


    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &uriPlayerObject, &audioSource,
                                                &audioSink, 7,
                                                ids, req);

    (*env)->ReleaseStringUTFChars(env, uri, utf8);

    result = (*uriPlayerObject)->Realize(uriPlayerObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) {
        (*uriPlayerObject)->Destroy(uriPlayerObject);
        uriPlayerObject = NULL;
        return JNI_FALSE;
    }


    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_PLAY, &uriPlayerPlay);
    checkError(result);
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_SEEK, &uriPlayerSeek);
    checkError(result);
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_MUTESOLO, &uriPlayerMuteSolo);
    checkError(result);
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_VOLUME, &uriPlayerVolume);
    checkError(result);
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_BASSBOOST, &uriBassBoost);
    checkError(result);
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_EQUALIZER, &eqOutputItf);
    checkError(result);
    result = (*uriPlayerObject)->GetInterface(uriPlayerObject, SL_IID_PREFETCHSTATUS, &uriPrefetchItf);
    checkError(result);

    result = (*uriPlayerPlay)->SetPlayState(uriPlayerPlay, SL_PLAYSTATE_PAUSED);
    checkError(result);

    /* Wait until there's data to play */
    SLuint32 prefetchStatus = SL_PREFETCHSTATUS_UNDERFLOW;
    while (prefetchStatus != SL_PREFETCHSTATUS_SUFFICIENTDATA) {
        usleep(100 * 1000);
        (*uriPrefetchItf)->GetPrefetchStatus(uriPrefetchItf, &prefetchStatus);
    }


//    result = (*uriPlayerPlay)->SetMarkerPosition(uriPlayerPlay, 2000);
//    checkError(result);
//    result = (*uriPlayerPlay)->SetPositionUpdatePeriod(uriPlayerPlay, 1000);
//    checkError(result);


    result = (*uriPlayerPlay)->RegisterCallback(uriPlayerPlay,
                                                playStatusCallback, 0);
    checkError(result);
    result = (*uriPlayerPlay)->SetCallbackEventsMask(uriPlayerPlay, SL_PLAYEVENT_HEADATMARKER |
                                                                    SL_PLAYEVENT_HEADATNEWPOS |
                                                                    SL_PLAYEVENT_HEADATEND);
    checkError(result);

    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setPlayingUriAudioPlayer(JNIEnv *env,
                                                                           jobject instance,
                                                                           jboolean isPlaying) {
    if (NULL != uriPlayerPlay) {
        SLresult result = (*uriPlayerPlay)->SetPlayState(uriPlayerPlay, isPlaying ?
                                                                        SL_PLAYSTATE_PLAYING
                                                                                  : SL_PLAYSTATE_PAUSED);
        checkError(result);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setVolumeUriAudioPlayer(JNIEnv *env,
                                                                          jobject instance,
                                                                          jint milliBel) {
    if (NULL != uriPlayerVolume) {
        SLresult result = (*uriPlayerVolume)->SetVolumeLevel(uriPlayerVolume, milliBel);
        checkError(result);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setSeekUriAudioPlayer(JNIEnv *env,
                                                                        jobject instance,
                                                                        jlong milliseconds) {
    if (NULL != uriPlayerSeek) {
        SLresult result = (*uriPlayerSeek)->SetPosition(uriPlayerSeek, milliseconds,
                                                        SL_SEEKMODE_FAST);
        checkError(result);
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

    if (outputMixObject != NULL) {
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
    }

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
//        checkError(result);
        return msec;
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getPosition(JNIEnv *env, jobject instance) {

    if (NULL != uriPlayerPlay) {

        SLmillisecond msec;
        SLresult result = (*uriPlayerPlay)->GetPosition(uriPlayerPlay, &msec);
//        checkError(result);
        return msec;
    }

    return 0;
}

SLuint32 getPlayState() {

    if (NULL != uriPlayerPlay) {

        SLuint32 state;
        SLresult result = (*uriPlayerPlay)->GetPlayState(uriPlayerPlay, &state);
        checkError(result);

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
                                                                  SL_BOOLEAN_TRUE
                                                                           : SL_BOOLEAN_FALSE);
        checkError(result);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_isEQEnabled(JNIEnv *env, jobject instance) {

    if (NULL != eqOutputItf) {
        SLboolean enabled = SL_BOOLEAN_FALSE;
        SLresult result = (*eqOutputItf)->IsEnabled(eqOutputItf, &enabled);
        checkError(result);
        return (jboolean) enabled;
    }

    return JNI_FALSE;

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_usePreset(JNIEnv *env, jobject instance,
                                                            jint presetValue) {

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->UsePreset(eqOutputItf, (SLuint16) presetValue);
        checkError(result);
    }
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getNumberOfBands(JNIEnv *env, jobject instance) {

    SLuint16 numberBands = 0;

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfBands(eqOutputItf, &numberBands);
        checkError(result);
    }
    return numberBands;
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getNumberOfPresets(JNIEnv *env,
                                                                     jobject instance) {

    SLuint16 numberPresets = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfPresets(eqOutputItf, &numberPresets);
        checkError(result);
    }
    return numberPresets;

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getCurrentPreset(JNIEnv *env, jobject instance) {

    SLuint16 preset = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetCurrentPreset(eqOutputItf, &preset);
        checkError(result);
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
        checkError(result);
    }
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getBandLevel(JNIEnv *env, jobject instance,
                                                               jint bandNumber) {
    SLmillibel level = NULL;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetBandLevel(eqOutputItf, (SLuint16) bandNumber, &level);
        checkError(result);
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
        checkError(result);
    }
    return centerFreq;

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_services_PlaybackService_getNumberOfPreset(JNIEnv *env, jobject instance) {

    SLuint16 numberPreset = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfPresets(eqOutputItf, &numberPreset);
        checkError(result);
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
        checkError(result);
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
        checkError(result);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_isSupportedBassBoost(JNIEnv *env,
                                                                       jobject instance) {
    if (NULL != uriBassBoost) {
        SLboolean strengthSupported = SL_BOOLEAN_FALSE;
        SLresult result = (*uriBassBoost)->IsStrengthSupported(uriBassBoost, &strengthSupported);
        checkError(result);
        return (jboolean) strengthSupported;
    }
    return JNI_FALSE;
}


JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setBassBoostValue(JNIEnv *env, jobject instance,
                                                                    jint value) {
    if (NULL != uriBassBoost) {
        SLresult result = (*uriBassBoost)->SetStrength(uriBassBoost, (SLuint16) value);
        checkError(result);
    }
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_isEnabledBassBoost(JNIEnv *env,
                                                                     jobject instance) {
    if (NULL != uriBassBoost) {
        SLboolean strengthSupported = SL_BOOLEAN_FALSE;
        SLresult result = (*uriBassBoost)->IsEnabled(uriBassBoost, &strengthSupported);
        checkError(result);
        return (jboolean) strengthSupported;
    }

    return JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_isSupportedVirtualizer(JNIEnv *env,
                                                                         jobject instance) {
    if (NULL != uriVirtualizer) {
        SLboolean strengthSupported = SL_BOOLEAN_FALSE;
        SLresult result = (*uriVirtualizer)->IsStrengthSupported(uriVirtualizer,
                                                                 &strengthSupported);
        checkError(result);
        return (jboolean) strengthSupported;
    }
    return JNI_FALSE;

}

JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_services_PlaybackService_isEnabledVirtualizer(JNIEnv *env,
                                                                       jobject instance) {

    if (NULL != uriVirtualizer) {
        SLboolean strengthSupported = SL_BOOLEAN_FALSE;
        SLresult result = (*uriVirtualizer)->IsEnabled(uriVirtualizer, &strengthSupported);
        checkError(result);
        return (jboolean) strengthSupported;
    }

    return JNI_FALSE;

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setEnableVirtualizer(JNIEnv *env,
                                                                       jobject instance,
                                                                       jboolean isEnable) {
    if (NULL != uriVirtualizer) {

        SLboolean enable = isEnable ?
                           SL_BOOLEAN_TRUE : SL_BOOLEAN_FALSE;
        SLresult result = (*uriVirtualizer)->SetEnabled(uriVirtualizer, enable);
        checkError(result);
    }

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setVirtualizerValue(JNIEnv *env, jobject instance,
                                                                      jint value) {
    if (NULL != uriVirtualizer) {
        SLresult result = (*uriVirtualizer)->SetStrength(uriVirtualizer, (SLuint16) value);
        checkError(result);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setMuteUriAudioPlayer(JNIEnv *env,
                                                                        jobject instance,
                                                                        jboolean mute) {
    if (NULL != uriPlayerVolume) {
        SLresult result = (*uriPlayerVolume)->SetMute(uriPlayerVolume, mute);
        checkError(result);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_enableStereoPositionUriAudioPlayer(JNIEnv *env,
                                                                                     jobject instance,
                                                                                     jboolean enable) {
    if (NULL != uriPlayerVolume) {
        SLresult result = (*uriPlayerVolume)->EnableStereoPosition(uriPlayerVolume, enable);
        checkError(result);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setStereoPositionUriAudioPlayer(JNIEnv *env,
                                                                                  jobject instance,
                                                                                  jint permille) {
    if (NULL != uriPlayerVolume) {
        SLresult result = (*uriPlayerVolume)->SetStereoPosition(uriPlayerVolume,
                                                                (SLpermille) permille);
        checkError(result);
    }
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_services_PlaybackService_setLoopingUriAudioPlayer(JNIEnv *env, jclass type,
                                                                           jboolean isLooping) {
    if (NULL != uriPlayerSeek) {

        SLresult result = (*uriPlayerSeek)->SetLoop(uriPlayerSeek, (SLboolean) isLooping, 0,
                                                    SL_TIME_UNKNOWN);
        checkError(result);
    }
}

