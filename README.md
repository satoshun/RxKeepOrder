[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.satoshun.RxKeepOrder/rxkeeporder/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.satoshun.RxKeepOrder/rxkeeporder)
[![CircleCI](https://circleci.com/gh/satoshun/RxKeepOrder.svg?style=svg)](https://circleci.com/gh/satoshun/RxKeepOrder)

# RxKeepOrder

keeps ordering of RxJava Streams(Observable, Flowable, Single and Maybe)

## motivation

concatArrayEager is so useful. But doesn't support multi observable type(Observable, Flowable, Completable and Maybe)

This library can be executed parallel and keep order emit items multi observable types!!


## usage

Java simple example.

```java
// set default observeOn scheduler, almost mainThread when Android Platform
seed = RxKeepOrder().setObserveScheduler(AndroidSchedulers.mainThread());

// evaluate parallel observable1, observable2 and observable3
// But evaluate subscribe keep order!!

observable1
    .compose(seed.attach())
    .subscribe();
observable2
    .compose(seed.attach())
    .subscribe();
observable3
    .compose(seed.attach())
    .subscribe();
...
```


Kotlin simple example.

```kotlln
// set default observeOn scheduler, almost mainThread when Android Platform
seed = RxKeepOrder().setObserveScheduler(AndroidSchedulers.mainThread());

// evaluate parallel observable1, observable2 and observable3
// But evaluate subscribe keep order!!

observable1
    .keepOrder(seed)
    .subscribe();
observable2
    .keepOrder(seed)
    .subscribe();
observable3
    .keepOrder(seed)
    .subscribe();
...
```

## install

```groovy
implementation 'com.github.satoshun.RxKeepOrder:rxkeeporder:0.3.0'

// kotlin
implementation 'com.github.satoshun.RxKeepOrder:rxkeeporder-kotlin:0.3.0'
```
