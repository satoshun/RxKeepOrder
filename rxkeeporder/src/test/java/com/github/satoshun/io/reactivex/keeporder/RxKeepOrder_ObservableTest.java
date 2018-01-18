package com.github.satoshun.io.reactivex.keeporder;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RxKeepOrder_ObservableTest {

  private RxKeepOrder rxKeepOrder;

  @Before public void setup() throws Exception {
    rxKeepOrder = new RxKeepOrder();
  }

  @Test public void observable__keep_order_two() throws Exception {
    Observable<Integer> test1 = Observable.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>attach())
        .subscribeOn(Schedulers.io());
    Observable<Integer> test2 = Observable.just(2)
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>attach())
        .subscribeOn(Schedulers.io());

    TestObserver<Integer> merged = Observable.merge(test1, test2).test();
    merged.await();
    merged.assertValues(1, 2);
  }

  @Test public void observable__keep_order_three() throws Exception {
    Observable<Integer> test1 = Observable.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>attach())
        .subscribeOn(Schedulers.io());
    Observable<Integer> test2 = Observable.just(2)
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>attach())
        .subscribeOn(Schedulers.io());
    Observable<Integer> test34 = Observable.just(3, 4)
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>attach())
        .subscribeOn(Schedulers.io());

    TestObserver<Integer> merged = Observable.merge(test1, test2, test34).test();
    merged.await();
    merged.assertValues(1, 2, 3, 4);
  }

  @Test public void observable__keep_order_difference_type() throws Exception {
    final CountDownLatch latch = new CountDownLatch(4);
    final List<Object> actual = new ArrayList<Object>();
    final List<Object> expected = new ArrayList<Object>(Arrays.asList(
        1,
        "2",
        Collections.singletonList("3"),
        Collections.singletonList("4")));
    Observable.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>attach())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Observable.just("2")
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<String>attach())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
          @Override public void accept(String value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Observable.just(
        Collections.singletonList("3"),
        Collections.singletonList("4"))
        .delay(10, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<List<String>>attach())
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

  @Test public void observable__keep_order_difference_type_with_error() throws Exception {
    final CountDownLatch latch = new CountDownLatch(5);
    final RuntimeException pseudoException = new RuntimeException("pseudo error");
    final List<Object> expected = new ArrayList<Object>(Arrays.asList(
        1,
        "2",
        pseudoException,
        Collections.singletonList("3"),
        Collections.singletonList("4")));
    final List<Object> actual = new ArrayList<Object>();
    Observable.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>attach())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Observable.just("2")
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<String>attach())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
          @Override public void accept(String value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Observable.error(pseudoException)
        .delay(5, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.attach())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Object>() {
          @Override public void accept(Object value) throws Exception {
            throw new IllegalArgumentException("not called it code");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable value) throws Exception {
            actual.add(value);
            latch.countDown();
          }
        });
    Observable.just(
        Collections.singletonList("3"),
        Collections.singletonList("4"))
        .delay(10, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<List<String>>attach())
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
