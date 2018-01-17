package com.github.satoshun.io.reactivex.keeporder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RxKeepOrder_CompletableTest {

  private RxKeepOrder rxKeepOrder;

  @Before public void setup() throws Exception {
    rxKeepOrder = new RxKeepOrder();
  }

  @Test public void completable_keep_order_two() throws Exception {
    final List<String> expected = Arrays.asList("1", "2");
    final List<String> actual = new ArrayList<String>();
    Completable test1 = Completable.complete()
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .doOnComplete(new Action() {
          @Override public void run() throws Exception {
            actual.add("1");
          }
        })
        .subscribeOn(Schedulers.io());
    Completable test2 = Completable.complete()
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .doOnComplete(new Action() {
          @Override public void run() throws Exception {
            actual.add("2");
          }
        })
        .subscribeOn(Schedulers.io());

    TestObserver<Void> merged = Completable.mergeArray(test1, test2).test();
    merged.await();
    compareList(actual, expected);
  }

  @Test public void completable_keep_order_three() throws Exception {
    final List<String> expected = Arrays.asList("1", "2", "3");
    final List<String> actual = new ArrayList<String>();
    Completable test1 = Completable.complete()
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .doOnComplete(new Action() {
          @Override public void run() throws Exception {
            actual.add("1");
          }
        })
        .subscribeOn(Schedulers.io());
    Completable test2 = Completable.complete()
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .doOnComplete(new Action() {
          @Override public void run() throws Exception {
            actual.add("2");
          }
        })
        .subscribeOn(Schedulers.io());

    Completable test3 = Completable.complete()
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .doOnComplete(new Action() {
          @Override public void run() throws Exception {
            actual.add("3");
          }
        })
        .subscribeOn(Schedulers.io());

    TestObserver<Void> merged = Completable.mergeArray(test1, test2, test3).test();
    merged.await();
    compareList(actual, expected);
  }

  @Test public void completable_keep_order_with_error() throws Exception {
    final CountDownLatch latch = new CountDownLatch(4);
    final RuntimeException pseudoException = new RuntimeException("pseudo error");
    final List expected = new ArrayList<Object>(Arrays.asList(
        "1",
        "2",
        pseudoException,
        "3"));
    final List<Object> actual = new ArrayList<Object>();
    Completable.complete()
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Action() {
          @Override public void run() throws Exception {
            actual.add("1");
            latch.countDown();
          }
        });
    Completable.complete()
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Action() {
          @Override public void run() throws Exception {
            actual.add("2");
            latch.countDown();
          }
        });

    Completable.error(pseudoException)
        .delay(5, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Action() {
          @Override public void run() throws Exception {
            throw new IllegalArgumentException("not called it code");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable throwable) throws Exception {
            actual.add(throwable);
            latch.countDown();
          }
        });

    Completable.complete()
        .delay(10, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Action() {
          @Override public void run() throws Exception {
            actual.add("3");
            latch.countDown();
          }
        });

    latch.await(1000, TimeUnit.MILLISECONDS);
    compareList(actual, expected);
  }

  private static <T> void compareList(List<T> actual, List<T> expected) {
    assertThat("no consume stream value", actual.size(), is(expected.size()));
    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i), is(expected.get(i)));
    }
  }
}
