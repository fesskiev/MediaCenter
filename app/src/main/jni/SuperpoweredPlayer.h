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

#define HEADROOM_DECIBEL 3.0f
static const float headroom = powf(10.0f, -HEADROOM_DECIBEL * 0.025f);

class SuperpoweredExample {
public:

	SuperpoweredExample(unsigned int samplerate, unsigned int buffersize, const char *path, int fileAoffset, int fileAlength, int fileBoffset, int fileBlength);
	~SuperpoweredExample();

	bool process(short int *output, unsigned int numberOfSamples);
	void onPlayPause(bool play);
	void onSeek(int value);
	void onCrossfader(int value);
	void onFxSelect(int value);
	void onFxOff();
	void onFxValue(int value);

private:
    pthread_mutex_t mutex;
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *playerA, *playerB;
    SuperpoweredRoll *roll;
    SuperpoweredFilter *filter;
    SuperpoweredFlanger *flanger;
    float *stereoBuffer;
    unsigned char activeFx;
    float crossValue, volA, volB;
};

#endif
