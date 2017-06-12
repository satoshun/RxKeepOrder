package com.github.satoshun.io.reactivex.keeporder;

import android.os.Looper;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
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

      @Override public Publisher<T> apply(Flowable<T> upstream) {
        verifyMainThread();
        Flowable<Object> singleEmissionFlowable = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL))
            .toFlowable();
        Flowable<Object> upNext = Flowable.concatArrayDelayError(
            singleEmissionFlowable, upstream
        ).skip(1).cache();
        preSource = upNext;
        return (Flowable<T>) upNext;
      }

      @Override public ObservableSource<T> apply(Observable<T> upstream) {
        verifyMainThread();
        Observable<Object> singleEmissionObservable = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL))
            .toObservable();
        Observable<Object> upNext = Observable.concatArrayDelayError(
            singleEmissionObservable, upstream
        ).skip(1).cache();
        preSource = upNext.toFlowable(BackpressureStrategy.DROP);
        return (ObservableSource<T>) upNext;
      }

      @Override public SingleSource<T> apply(Single<T> upstream) {
        verifyMainThread();
        Single<Object> singleEmissionSingle = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL));
        Flowable<Object> upNext = Single.concatArray(
            singleEmissionSingle, upstream
        ).skip(1).cache();
        preSource = upNext;
        return (SingleSource<T>) upNext.singleOrError();
      }

      @Override public MaybeSource<T> apply(Maybe<T> upstream) {
        verifyMainThread();
        Maybe<Object> singleEmissionMaybe = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL))
            .toMaybe();
        Flowable<Object> upNext = Maybe.concatArrayDelayError(
            singleEmissionMaybe, upstream
        ).skip(1).cache();
        preSource = upNext;
        return (MaybeSource<T>) upNext.singleElement();
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
