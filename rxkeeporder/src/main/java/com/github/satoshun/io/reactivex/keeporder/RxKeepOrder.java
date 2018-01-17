package com.github.satoshun.io.reactivex.keeporder;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.internal.functions.Functions;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Flowable.bufferSize;

@SuppressWarnings("unchecked")
public class RxKeepOrder {

  private static final Object SENTINEL = new Object();

  private Scheduler scheduler = Schedulers.newThread();

  private Flowable<Object> preSource = Flowable.empty();

  @NonNull public <T> KeepOrderTransformer<T> apply() {
    return new KeepOrderTransformer<T>() {

      @Override public Publisher<T> apply(Flowable<T> upstream) {
        Flowable<Object> singleEmission = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL))
            .toFlowable();
        Flowable<Object> upNext = Flowable.fromArray(singleEmission, upstream).concatMapEagerDelayError(
            (Function) Functions.identity(), bufferSize(), bufferSize(), false
        ).skip(1).observeOn(scheduler).cache();
        preSource = upNext;
        return (Flowable<T>) upNext;
      }

      @Override public ObservableSource<T> apply(Observable<T> upstream) {
        Observable<Object> singleEmission = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL))
            .toObservable();
        Observable<Object> upNext = Observable.concatArrayEager(
            singleEmission, upstream
        ).skip(1).observeOn(scheduler).cache();
        preSource = upNext.toFlowable(BackpressureStrategy.DROP);
        return (ObservableSource<T>) upNext;
      }

      @Override public SingleSource<T> apply(Single<T> upstream) {
        Observable<Object> singleEmission = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL))
            .toObservable();
        Observable<Object> upNext = Observable.concatArrayEager(
            singleEmission, upstream.toObservable()
        ).skip(1).observeOn(scheduler).observeOn(scheduler).cache();
        preSource = upNext.toFlowable(BackpressureStrategy.DROP);
        return (SingleSource<T>) upNext.singleOrError();
      }

      @Override public MaybeSource<T> apply(Maybe<T> upstream) {
        Observable<Object> singleEmission = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL))
            .toObservable();
        Observable<Object> upNext = Observable.concatArrayEager(
            singleEmission, upstream.toObservable()
        ).skip(1).observeOn(scheduler).cache();
        preSource = upNext.toFlowable(BackpressureStrategy.DROP);
        return (MaybeSource<T>) upNext.singleElement();
      }

      @Override public CompletableSource apply(Completable upstream) {
        Observable<Object> singleEmission = preSource
            .lastOrError()
            .onErrorResumeNext(Single.just(SENTINEL))
            .toObservable();
        Observable<Object> upNext = Observable.concatArrayEager(
            singleEmission, upstream.toObservable()
        ).skip(1).observeOn(scheduler).cache();
        preSource = upNext.toFlowable(BackpressureStrategy.DROP);
        return Completable.fromObservable(upNext);
      }
    };
  }

  public void setObserveScheduler(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  public void clear() {
    preSource = Flowable.empty();
  }
}
