#ifndef Header_SuperpoweredExample
#define Header_SuperpoweredExample

#include <math.h>
#include <pthread.h>
#include <Superpowered3BandEQ.h>
#include <SuperpoweredMixer.h>

#include "SuperpoweredPlayer.h"
#include "SuperpoweredAdvancedAudioPlayer.h"
#include "SuperpoweredFilter.h"
#include "SuperpoweredRoll.h"
#include "AndroidIO/SuperpoweredAndroidAudioIO.h"
#include "SuperpoweredFlanger.h"


class SuperpoweredPlayer {

public:

    SuperpoweredPlayer(unsigned int samplerate, unsigned int buffersize);

    ~SuperpoweredPlayer();

    bool process(short int *output, unsigned int numberOfSamples);
    void togglePlayback();
    void setVolume(float value);
    void setSeek(int value);
    unsigned int getDuration();
    unsigned int getPosition();
    float getVolume();
    float getPositionPercent();
    bool isPlaying();
    bool isLooping();
    bool isEnableEQ();
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

    float left;
    float right;
};

#endif
