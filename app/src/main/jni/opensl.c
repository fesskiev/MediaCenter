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
static SLEqualizerItf outputMixEQ = NULL;

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
                                              &outputMixEQ);

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
        outputMixEQ = NULL;
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

