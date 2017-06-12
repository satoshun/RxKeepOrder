package com.github.satoshun.io.reactivex.keeporder;

import android.os.Looper;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;

public class RxKeepOrder {

  private static final Object SENTINEL = new Object();

  private Flowable<Object> preSource = Flowable.empty();

  @NonNull public <T> KeepOrderTransformer<T> apply() {
    return new KeepOrderTransformer<T>() {

      @Override public ObservableSource<T> apply(Observable<T> upstream) {
        verifyMainThread();
        Observable<Object> singleEmissionObservable = preSource
            .onErrorResumeNext(Flowable.empty())
            .last(SENTINEL)
            .toObservable();
        Observable<Object> upNext = Observable.concatArrayEager(
            singleEmissionObservable, upstream
        ).skip(1).cache();
        preSource = upNext.toFlowable(BackpressureStrategy.DROP);
        return (ObservableSource<T>) upNext;
      }

      @Override public SingleSource<T> apply(Single<T> upstream) {
        verifyMainThread();
        Single<Object> singleEmissionObservable = preSource
            .onErrorResumeNext(Flowable.empty())
            .last(SENTINEL);
        Flowable<Object> upNext = Single.concatArray(
            singleEmissionObservable, upstream
        ).skip(1).cache();
        preSource = upNext;
        return (SingleSource<T>) upNext.singleOrError();
      }
    };
  }

  private static void verifyMainThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("Expected to be called on the main thread but was " + Thread.currentThread().getName());
    }
  }

  public void clear() {
    preSource = Flowable.empty();
  }
}
