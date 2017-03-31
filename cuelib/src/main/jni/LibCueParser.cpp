#include <stdio.h>
#include <jni.h>
#include "libcue.h"


extern "C" JNIEXPORT void
Java_com_fesskiev_cue_CueParser_parseCue(JNIEnv *env, jobject instance, jcharArray data_) {

    jchar *data = env->GetCharArrayElements(data_, NULL);

    Cd *cd = cue_parse_string((const char *) data);
    Rem *rem = cd_get_rem(cd);
    Cdtext *cdtext = cd_get_cdtext(cd);

    const char *val;
    val = cdtext_get(PTI_PERFORMER, cdtext);
    val = cdtext_get(PTI_TITLE, cdtext);
    val = cdtext_get(PTI_GENRE, cdtext);
    val = rem_get(REM_DATE, rem);

    int ival = cd_get_ntrack(cd);

    Track *track;
    track = cd_get_track(cd, 1);
    val = track_get_filename(track);

    cdtext = track_get_cdtext(track);
    val = cdtext_get(PTI_PERFORMER, cdtext);
    val = cdtext_get(PTI_TITLE, cdtext);
    ival = track_get_start(track);
    ival = track_get_length(track);
    ival = track_get_index(track, 1);

    cd_delete(cd);

    env->ReleaseCharArrayElements(data_, data, 0);
}