package com.fesskiev.player.utils;




import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscription;

public class RxUtils {

    public static <T> Observable<T> fromCallable(Callable<T> callable) {
        return Observable.fromCallable(callable);
    }


    public static void unsubscribe(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
