package com.fesskiev.mediacenter.utils;


import android.util.Log;

import com.fesskiev.mediacenter.BuildConfig;

public class AppLog {

    private static final String TAG = AppLog.class.getSimpleName();

    private enum TYPE {
        WTF,
        DEBUG,
        ERROR,
        INFO,
        VERBOSE
    }

    public static void ERROR(String message) {
        Log(TYPE.ERROR, message);
    }

    public static void DEBUG(String message) {
        Log(TYPE.DEBUG, message);
    }

    public static void VERBOSE(String message) {
        Log(TYPE.VERBOSE, message);
    }

    public static void INFO(String message) {
        Log(TYPE.INFO, message);
    }

    public static void WTF(String message) {
        Log(TYPE.WTF, message);
    }


    private static void Log(TYPE type, String message) {
        if (BuildConfig.DEBUG) {
            switch (type){
                case WTF:
                    Log.wtf(TAG, message);
                    break;
                case DEBUG:
                    Log.d(TAG, message);
                    break;
                case ERROR:
                    Log.e(TAG, message);
                    break;
                case INFO:
                    Log.i(TAG, message);
                    break;
                case VERBOSE:
                    Log.v(TAG, message);
                    break;
            }
        }
    }
}
