package com.fesskiev.mediacenter.utils;




import android.os.Looper;

import io.reactivex.disposables.Disposable;

public class RxUtils {


    public static void unsubscribe(Disposable subscription) {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }

    public static void RxLog(String text) {
        if (isOnMainThread()) {
            AppLog.DEBUG(String.format("%s <MAIN THREAD>", text));
        } else {
            AppLog.DEBUG(String.format(String.format("%s in <NOT MAIN THREAD>", text)));
        }
    }

    private static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
