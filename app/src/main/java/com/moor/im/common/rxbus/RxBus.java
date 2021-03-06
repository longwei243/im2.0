package com.moor.im.common.rxbus;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by longwei on 2016/4/6.
 */
public class RxBus {

    private static RxBus instance;

    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {
        _bus.onNext(o);
    }

    public Observable<Object> toObserverable() {
        return _bus;
    }

    public boolean hasObservers() {
        return _bus.hasObservers();
    }

    private RxBus() {}

    public static RxBus getInstance() {
        if(instance == null) {
            synchronized (RxBus.class) {
                if(instance == null) {
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }
}
