#ifndef Header_SuperpoweredExample
#define Header_SuperpoweredExample

#include <math.h>
#include <pthread.h>

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


private:
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *player;
    float *buffer;
    float volume;
};

#endif
