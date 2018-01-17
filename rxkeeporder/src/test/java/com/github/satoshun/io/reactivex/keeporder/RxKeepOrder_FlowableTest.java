package com.github.satoshun.io.reactivex.keeporder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RxKeepOrder_FlowableTest {

  private RxKeepOrder rxKeepOrder;

  @Before public void setup() throws Exception {
    rxKeepOrder = new RxKeepOrder();
  }

  @Test public void flowable__keep_order_two() throws Exception {
    Flowable<Integer> test1 = Flowable.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());
    Flowable<Integer> test2 = Flowable.just(2)
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());

    TestSubscriber<Integer> merged = Flowable.merge(test1, test2).test();
    merged.await();
    merged.assertValues(1, 2);
  }

  @Test public void flowable__keep_order_difference_type() throws Exception {
    final CountDownLatch latch = new CountDownLatch(4);
    final List<Object> actual = new ArrayList<Object>();
    final List<Object> expected = new ArrayList<Object>(Arrays.asList(
        1,
        "2",
        Collections.singletonList("3"),
        Collections.singletonList("4")));

    Flowable.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Flowable.just("2")
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<String>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
          @Override public void accept(String value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Flowable.just(
        Collections.singletonList("3"),
        Collections.singletonList("4"))
        .delay(10, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<List<String>>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<List<String>>() {
          @Override public void accept(List<String> value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    latch.await(1000, TimeUnit.MILLISECONDS);
    assertThat("no consume stream value", actual.size(), is(expected.size()));
    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i), is(expected.get(i)));
    }
  }

  @Test public void flowable__keep_order_difference_type_with_error() throws Exception {
    final CountDownLatch latch = new CountDownLatch(5);
    final RuntimeException pseudoException = new RuntimeException("pseudo error");
    final List<Object> actual = new ArrayList<Object>();
    final List<Object> expected = new ArrayList<Object>(Arrays.asList(
        1,
        "2",
        pseudoException,
        Collections.singletonList("3"),
        Collections.singletonList("4")));
    Flowable.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Flowable.just("2")
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<String>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
          @Override public void accept(String value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Flowable.error(pseudoException)
        .delay(5, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Object>() {
          @Override public void accept(Object value) throws Exception {
            throw new IllegalArgumentException("not reach it code");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Flowable.just(
        Collections.singletonList("3"),
        Collections.singletonList("4"))
        .delay(10, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<List<String>>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<List<String>>() {
          @Override public void accept(List<String> value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    latch.await(1000, TimeUnit.MILLISECONDS);
    assertThat("no consume stream value", actual.size(), is(expected.size()));
    for (int i = 0; i < actual.size(); i++) {
      assertThat(actual.get(i), is(expected.get(i)));
    }
  }
}
