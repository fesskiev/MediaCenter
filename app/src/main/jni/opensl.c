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


JNIEXPORT void JNICALL
Java_com_fesskiev_player_MusicApplication_createEngine(JNIEnv *env, jclass type) {

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

void playStatusCallback(SLPlayItf play, void *context, SLuint32 event) {
    __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native", "The value o is %d", event);
}


JNIEXPORT jboolean JNICALL
Java_com_fesskiev_player_MusicApplication_createUriAudioPlayer(JNIEnv *env, jclass type,
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
Java_com_fesskiev_player_MusicApplication_setPlayingUriAudioPlayer(JNIEnv *env, jclass type,
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
Java_com_fesskiev_player_MusicApplication_setVolumeUriAudioPlayer(JNIEnv *env, jclass type,
                                                                  jint millibel) {
    SLresult result;
    if (NULL != uriPlayerVolume) {
        result = (*uriPlayerVolume)->SetVolumeLevel(uriPlayerVolume, millibel);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_MusicApplication_releaseEngine(JNIEnv *env, jclass type) {


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

JNIEXPORT void JNICALL
Java_com_fesskiev_player_MusicApplication_setSeekUriAudioPlayer(JNIEnv *env, jclass type,
                                                                jlong milliseconds) {
    SLresult result;

    if (NULL != uriPlayerSeek) {

        result = (*uriPlayerSeek)->SetPosition(uriPlayerSeek, milliseconds, SL_SEEKMODE_FAST);

        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }

}


JNIEXPORT jint JNICALL
Java_com_fesskiev_player_MusicApplication_getDuration(JNIEnv *env, jclass type) {

    if (NULL != uriPlayerPlay) {

        SLmillisecond msec;
        SLresult result = (*uriPlayerPlay)->GetDuration(uriPlayerPlay, &msec);

        assert(SL_RESULT_SUCCESS == result);
        return msec;
    }

    return 0;

}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_MusicApplication_getPosition(JNIEnv *env, jclass type) {

    if (NULL != uriPlayerPlay) {

        SLmillisecond msec;
        SLresult result = (*uriPlayerPlay)->GetPosition(uriPlayerPlay, &msec);
        assert(SL_RESULT_SUCCESS == result);
        return msec;
    }

    return 0;

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_MusicApplication_releaseUriAudioPlayer(JNIEnv *env, jclass type) {
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
Java_com_fesskiev_player_MusicApplication_isPlaying(JNIEnv *env, jclass type) {

    return getPlayState() == SL_PLAYSTATE_PLAYING;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_MusicApplication_testEQ(JNIEnv *env, jclass type) {
    SLresult result;
    /* Configure EQ */
    SLuint16 nbPresets, preset, nbBands = 0;
    result = (*eqOutputItf)->GetNumberOfBands(eqOutputItf, &nbBands);
    assert(SL_RESULT_SUCCESS == result);

    result = (*eqOutputItf)->GetNumberOfPresets(eqOutputItf, &nbPresets);
    assert(SL_RESULT_SUCCESS == result);
    /*    Start from a preset  */
    preset = nbPresets > 2 ? 2 : 0;
    result = (*eqOutputItf)->UsePreset(eqOutputItf, preset);

    preset = 1977;
    result = (*eqOutputItf)->GetCurrentPreset(eqOutputItf, &preset);
    assert(SL_RESULT_SUCCESS == result);

    if (SL_EQUALIZER_UNDEFINED == preset) {
        __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                            "Using SL_EQUALIZER_UNDEFINED preset, unexpected here!\n");
    } else {
        __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                            "Using preset %d\n", preset);
    }

    /*    Tweak it so it's obvious it gets turned on/off later */
    SLmillibel minLevel, maxLevel = 0;
    result = (*eqOutputItf)->GetBandLevelRange(eqOutputItf, &minLevel, &maxLevel);
    assert(SL_RESULT_SUCCESS == result);

    __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                        "Band level range = %dmB to %dmB\n", minLevel, maxLevel);


    SLuint16 b = 0;
    for (b = 0; b < nbBands / 2; b++) {
        result = (*eqOutputItf)->SetBandLevel(eqOutputItf, b, minLevel);
        assert(SL_RESULT_SUCCESS == result);
    }
    for (b = nbBands / 2; b < nbBands; b++) {
        result = (*eqOutputItf)->SetBandLevel(eqOutputItf, b, maxLevel);
        assert(SL_RESULT_SUCCESS == result);
    }

    SLmillibel level = 0;
    for (b = 0; b < nbBands; b++) {
        result = (*eqOutputItf)->GetBandLevel(eqOutputItf, b, &level);
        assert(SL_RESULT_SUCCESS == result);
        __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                            "Band %d level = %dmB\n", b, level);
    }

    SLmilliHertz minFreg;
    SLmilliHertz maxFreg;
    SLmilliHertz centerFreg;
    for (b = 0; b < nbBands; b++) {
        (*eqOutputItf)->GetBandFreqRange(eqOutputItf, b, &minFreg, &maxFreg);

        (*eqOutputItf)->GetCenterFreq(eqOutputItf, b, &centerFreg);

        __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                            "cnterCeBand %d freg = %dHz", b, centerFreg / 1000);

        __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                            "Band %d freg = %dHz to %dHz\n", b, minFreg / 1000, maxFreg / 1000);
    }

    SLuint16 numberPreset;
    const SLchar *namePreset;
    (*eqOutputItf)->GetNumberOfPresets(eqOutputItf, &numberPreset);
    for (b = 0; b < numberPreset; b++) {
        (*eqOutputItf)->GetPresetName(eqOutputItf, b, &namePreset);

        __android_log_print(ANDROID_LOG_VERBOSE, "OpenSl native",
                            "preset %d name %s", b, namePreset);
    }

    /* Switch EQ on/off every TIME_S_BETWEEN_EQ_ON_OFF seconds */
    SLboolean enabled = SL_BOOLEAN_TRUE;
    result = (*eqOutputItf)->SetEnabled(eqOutputItf, enabled);

}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_MusicApplication_setEnableEQ(JNIEnv *env, jclass type, jboolean isEnable) {

    SLresult result;
    if (NULL != eqOutputItf) {

        result = (*eqOutputItf)->SetEnabled(eqOutputItf, isEnable ?
                                                         SL_BOOLEAN_TRUE : SL_BOOLEAN_FALSE);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
    }

}


JNIEXPORT jint JNICALL
Java_com_fesskiev_player_MusicApplication_getNumberOfBands(JNIEnv *env, jclass type) {
    SLuint16 numberBands = 0;

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfBands(eqOutputItf, &numberBands);
        assert(SL_RESULT_SUCCESS == result);
    }

    return numberBands;
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_MusicApplication_getNumberOfPresets(JNIEnv *env, jclass type) {

    SLuint16 numberPresets = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfPresets(eqOutputItf, &numberPresets);
        assert(SL_RESULT_SUCCESS == result);
    }
    return numberPresets;
}

JNIEXPORT void JNICALL
Java_com_fesskiev_player_MusicApplication_usePreset(JNIEnv *env, jclass type, jint presetValue) {

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->UsePreset(eqOutputItf, (SLuint16) presetValue);
        assert(SL_RESULT_SUCCESS == result);
    }
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_MusicApplication_getCurrentPreset(JNIEnv *env, jclass type) {

    SLuint16 preset = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetCurrentPreset(eqOutputItf, &preset);
        assert(SL_RESULT_SUCCESS == result);
    }
    return preset;
}

JNIEXPORT jintArray JNICALL
Java_com_fesskiev_player_MusicApplication_getBandLevelRange(JNIEnv *env, jclass type) {
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
Java_com_fesskiev_player_MusicApplication_setBandLevel(JNIEnv *env, jclass type, jint bandNumber,
                                                       jint milliBel) {

    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->SetBandLevel(eqOutputItf, (SLuint16) bandNumber,
                                                       (SLmillibel) milliBel);
        assert(SL_RESULT_SUCCESS == result);
    }
}

JNIEXPORT jint JNICALL
Java_com_fesskiev_player_MusicApplication_getBandLevel(JNIEnv *env, jclass type, jint bandNumber) {

    SLmillibel level;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetBandLevel(eqOutputItf, (SLuint16) bandNumber, &level);
        assert(SL_RESULT_SUCCESS == result);
    }
    return level;
}

JNIEXPORT jintArray JNICALL
Java_com_fesskiev_player_MusicApplication_getBandFrequencyRange(JNIEnv *env, jclass type,
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
Java_com_fesskiev_player_MusicApplication_getCenterFrequency(JNIEnv *env, jclass type,
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
Java_com_fesskiev_player_MusicApplication_getNumberOfPreset(JNIEnv *env, jclass type) {

    SLuint16 numberPreset = 0;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetNumberOfPresets(eqOutputItf, &numberPreset);
        assert(SL_RESULT_SUCCESS == result);
    }
    return numberPreset;
}

JNIEXPORT jstring JNICALL
Java_com_fesskiev_player_MusicApplication_getPresetName(JNIEnv *env, jclass type,
                                                        jint presetNumber) {
    const SLchar *namePreset = NULL;
    if (NULL != eqOutputItf) {
        SLresult result = (*eqOutputItf)->GetPresetName(eqOutputItf, (SLuint16) presetNumber,
                                                        &namePreset);
        assert(SL_RESULT_SUCCESS == result);
    }

    return (*env)->NewStringUTF(env, namePreset);
}


