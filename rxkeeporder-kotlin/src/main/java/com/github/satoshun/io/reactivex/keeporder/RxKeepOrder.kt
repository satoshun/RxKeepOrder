package com.github.satoshun.io.reactivex.keeporder

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

fun <T> Flowable<T>.keepOrder(seed: RxKeepOrder): Flowable<T> = this.compose(seed.attach())
fun <T> Observable<T>.keepOrder(seed: RxKeepOrder): Observable<T> = this.compose(seed.attach())
fun <T> Single<T>.keepOrder(seed: RxKeepOrder): Single<T> = this.compose(seed.attach())
fun <T> Maybe<T>.keepOrder(seed: RxKeepOrder): Maybe<T> = this.compose(seed.attach())
fun Completable.keepOrder(seed: RxKeepOrder): Completable = this.compose(seed.attach<Any>())
