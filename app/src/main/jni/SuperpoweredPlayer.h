#ifndef Header_SuperpoweredExample
#define Header_SuperpoweredExample

#include <math.h>
#include <pthread.h>
#include <Superpowered3BandEQ.h>
#include <SuperpoweredMixer.h>

#include "SuperpoweredPlayer.h"
#include "Superpowered/SuperpoweredAdvancedAudioPlayer.h"
#include "Superpowered/SuperpoweredFilter.h"
#include "Superpowered/SuperpoweredRoll.h"
#include "Superpowered/AndroidIO/SuperpoweredAndroidAudioIO.h"
#include "Superpowered/SuperpoweredFlanger.h"


class SuperpoweredPlayer {

public:

    SuperpoweredPlayer(unsigned int samplerate, unsigned int buffersize, const char *path);

    ~SuperpoweredPlayer();

    bool process(short int *output, unsigned int numberOfSamples);
    void setPlaying(bool isPlaying);
    void setVolume(float value);
    void setSeek(int value);
    int getDuration();
    int getPosition();
    bool isPlaying();
    void setLooping(bool looping);
    void open(const char *path);
    void setEQBands(int index, int value);
    void enableEQ(bool enable);
    void onForeground();
    void onBackground();


private:
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *player;
    Superpowered3BandEQ *bandEQ;
    SuperpoweredStereoMixer *mixer;
    float *buffer;
    float volume;
};

#endif
