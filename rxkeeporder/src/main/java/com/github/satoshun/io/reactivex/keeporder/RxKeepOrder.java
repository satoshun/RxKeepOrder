package com.github.satoshun.io.reactivex.keeporder;

import android.os.Looper;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;

public class RxKeepOrder {

  private static final Object SENTINEL = new Object();

  private Observable<Object> preSource = Observable.empty();

  @NonNull public <T> ObservableTransformer<T, T> apply() {
    return new ObservableTransformer<T, T>() {
      @Override public ObservableSource<T> apply(Observable<T> upstream) {
        verifyMainThread();
        Observable<Object> singleEmissionObservable = preSource
            .onErrorResumeNext(Observable.empty())
            .last(SENTINEL)
            .toObservable();
        Observable<Object> upNext = Observable.concatArrayEager(
            singleEmissionObservable, upstream
        ).skip(1).cache();
        preSource = upNext;
        return (ObservableSource<T>) upNext;
      }
    };
  }

  private static void verifyMainThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().getName());
    }
  }

  public void clear() {
    preSource = Observable.empty();
  }
}
