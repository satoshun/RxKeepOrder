package com.github.satoshun.io.reactivex.keeporder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RxKeepOrder_MaybeTest {

  private RxKeepOrder rxKeepOrder;

  @Before public void setup() throws Exception {
    rxKeepOrder = new RxKeepOrder();
  }

  @Test public void maybe__keep_order_two() throws Exception {
    Maybe<Integer> test1 = Maybe.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());
    Maybe<Integer> test2 = Maybe.just(2)
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());

    TestSubscriber<Integer> merged = Maybe.merge(test1, test2).test();
    merged.await();
    merged.assertValues(1, 2);
  }

  @Test public void maybe__keep_order_difference_type() throws Exception {
    final CountDownLatch latch = new CountDownLatch(3);
    final List<Object> actual = new ArrayList<Object>();
    final List<Object> expected = new ArrayList<Object>(Arrays.asList(
        1,
        "2",
        Collections.singletonList("3")));

    Maybe.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Maybe.just("2")
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<String>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
          @Override public void accept(String value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Maybe.just(Collections.singletonList("3"))
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

  @Test public void maybe__keep_order_difference_type_with_error() throws Exception {
    final CountDownLatch latch = new CountDownLatch(4);
    final RuntimeException pseudoException = new RuntimeException("pseudo error");
    final List<Object> actual = new ArrayList<Object>();
    final List<Object> expected = new ArrayList<Object>(Arrays.asList(
        1,
        "2",
        pseudoException,
        Collections.singletonList("3")));
    Maybe.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Maybe.just("2")
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<String>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
          @Override public void accept(String value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Maybe.error(pseudoException)
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
    Maybe.just(Collections.singletonList("3"))
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
