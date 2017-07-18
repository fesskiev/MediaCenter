#ifndef Header_SuperpoweredExample
#define Header_SuperpoweredExample

#include <math.h>
#include <pthread.h>
#include <Superpowered3BandEQ.h>
#include <SuperpoweredReverb.h>
#include <SuperpoweredEcho.h>
#include <SuperpoweredMixer.h>
#include <SuperpoweredGate.h>
#include <SuperpoweredWhoosh.h>
#include <SuperpoweredRecorder.h>

#include "SuperpoweredPlayer.h"
#include "SuperpoweredAdvancedAudioPlayer.h"
#include "AndroidIO/SuperpoweredAndroidAudioIO.h"


class SuperpoweredPlayer {

public:

    SuperpoweredPlayer(unsigned int samplerate, unsigned int buffersize,
                       const char *recorderTempPath);

    ~SuperpoweredPlayer();

    bool process(short int *output, unsigned int numberOfSamples);

    void togglePlayback();
    void setVolume(float value);
    void setSeek(int value);
    void setPosition(int value);
    unsigned int getDuration();
    unsigned int getPosition();
    float getVolume();
    float getPositionPercent();
    bool isPlaying();
    bool isLooping();
    void setLooping(bool looping);
    void open(const char *path);

    void setTempo(double tempo);
    void setPitchShift(int pitchShift);

    void startRecording(const char *destinationPath);
    void stopRecording();

    void setEQBands(int index, int value);
    void enableEQ(bool enable);
    bool isEnableEQ();

    void reverbValue(int mix, int width, int damp, int roomSize);
    void enableReverb(bool enable);
    bool isEnableReverb();

    void echoValue(int value);
    void enableEcho(bool enable);
    bool isEnableEcho();

    void whooshValue(int wet, int frequency);
    void enableWhoosh(bool enable);
    bool isEnableWhoosh();

    void loopBetween(double startMs, double endMs);
    void loopExit();

    void onForeground();
    void onBackground();


private:
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredRecorder *recorder;
    SuperpoweredAdvancedAudioPlayer *player;
    Superpowered3BandEQ *bandEQ;
    SuperpoweredStereoMixer *mixer;
    SuperpoweredReverb *reverb;
    SuperpoweredEcho *echo;
    SuperpoweredGate *gate;
    SuperpoweredWhoosh *whoosh;

    float *buffer;
    float *bufferRecording;
    float volume;
    bool record = false;

    float left;
    float right;
};

#endif
