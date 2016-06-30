package com.fesskiev.player.utils;


import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

public class RxUtils {

    public static <T> Observable<T> fromCallableObservable(Callable<T> callable) {
        return Observable.fromCallable(callable);
    }


    public static void unsubscribe(Subscription subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
