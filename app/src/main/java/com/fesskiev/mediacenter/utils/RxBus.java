package com.fesskiev.mediacenter.utils;


import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public final class RxBus {

    private final PublishSubject<Object> bus = PublishSubject.create();

    public void send(final Object event) {
        bus.onNext(event);
    }

    public Observable<Object> toObservable() {
        return bus;
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }
}