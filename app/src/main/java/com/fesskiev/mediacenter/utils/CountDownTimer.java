package com.fesskiev.mediacenter.utils;

import android.os.Handler;
import android.util.Log;


public class CountDownTimer {

    private long millisInFuture;
    private long countDownInterval;
    private volatile boolean pause;
    private Handler handler;
    private Runnable counter;


    private final static int ONE_SECOND = 1000;

    public interface OnCountDownListener {

        void tickCount();
    }

    private OnCountDownListener listener;

    public CountDownTimer(long countDownInterval) {
        this.countDownInterval = countDownInterval;

        initialize();

    }

    public void setOnCountDownListener(OnCountDownListener l) {
        this.listener = l;
    }

    public void stop() {
        handler.removeCallbacks(counter);
    }

    public void tick() {
        pause = false;
    }

    public void pause() {
        pause = true;
        this.millisInFuture = ONE_SECOND;
    }

    public boolean isStart() {
        return pause;
    }

    private void initialize() {
        handler = new Handler();
        counter = new Runnable() {

            public void run() {
                if (pause && millisInFuture > 0) {
                    millisInFuture -= countDownInterval;
                    Log.v("count_down", millisInFuture + " milliseconds remain");
                }

                if (pause && millisInFuture <= 0) {
//                    Log.v("count_down", "PAUSE!");
                } else {
                    if (listener != null) {
                        listener.tickCount();
                    }
                }
                handler.postDelayed(this, countDownInterval);
            }
        };
        handler.postDelayed(counter, countDownInterval);
    }
}