package com.github.satoshun.rxkeeporder;

import com.github.satoshun.io.reactivex.keeporder.BuildConfig;
import com.github.satoshun.io.reactivex.keeporder.RxKeepOrder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class RxKeepOrder_SingleTest {

  private RxKeepOrder rxKeepOrder;

  @Before public void setup() throws Exception {
    rxKeepOrder = new RxKeepOrder();
  }

  @Test public void single__keep_order_two() throws Exception {
    Single<Integer> test1 = Single.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());
    Single<Integer> test2 = Single.just(2)
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());

    TestSubscriber<Integer> merged = Single.merge(test1, test2).test();
    merged.await();
    merged.assertValues(1, 2);
  }

  @Test public void single__keep_order_three() throws Exception {
    Single<Integer> test1 = Single.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());
    Single<Integer> test2 = Single.just(2)
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());
    Single<Integer> test3 = Single.just(3)
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io());

    TestSubscriber<Integer> merged = Single.merge(test1, test2, test3).test();
    merged.await();
    merged.assertValues(1, 2, 3);
  }

  @Test public void single__keep_order_difference_type() throws Exception {
    final CountDownLatch latch = new CountDownLatch(3);
    final List<Object> expected = new ArrayList<>(Arrays.asList(
        1,
        "2",
        Collections.singletonList("3")));
    Single.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) throws Exception {
            assertThat((Integer) expected.remove(0), is(value));
            latch.countDown();
          }
        });
    Single.just("2")
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<String>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
          @Override public void accept(String value) throws Exception {
            assertThat((String) expected.remove(0), is(value));
            latch.countDown();
          }
        });
    Single.just(
        Collections.singletonList("3"))
        .delay(10, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<List<String>>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<List<String>>() {
          @Override public void accept(List<String> value) throws Exception {
            assertThat((List<String>) expected.remove(0), is(value));
            latch.countDown();
          }
        });
    latch.await(1000, TimeUnit.MILLISECONDS);
    assertThat("no consume stream value", expected.size(), is(0));
  }

  @Test public void single__keep_order_difference_type_with_error() throws Exception {
    final CountDownLatch latch = new CountDownLatch(4);
    final List<Object> expected = new ArrayList<>(Arrays.asList(
        1,
        "2",
        new RuntimeException("pseudo error"),
        Collections.singletonList("3")));
    Single.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) throws Exception {
            assertThat((Integer) expected.remove(0), is(value));
            latch.countDown();
          }
        });
    Single.just("2")
        .delay(30, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<String>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<String>() {
          @Override public void accept(String value) throws Exception {
            assertThat((String) expected.remove(0), is(value));
            latch.countDown();
          }
        });
    Single.error(new RuntimeException("pseudo error"))
        .delay(5, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Object>() {
          @Override public void accept(Object value) throws Exception {
            throw new IllegalArgumentException("not called it code");
          }
        }, new Consumer<Throwable>() {
          @Override public void accept(Throwable value) throws Exception {
            assertThat(
                ((Throwable) expected.remove(0)).getMessage(),
                is(value.getMessage()));
            latch.countDown();
          }
        });
    Single.just(
        Collections.singletonList("3"))
        .delay(10, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<List<String>>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<List<String>>() {
          @Override public void accept(List<String> value) throws Exception {
            assertThat((List<String>) expected.remove(0), is(value));
            latch.countDown();
          }
        });
    latch.await(1000, TimeUnit.MILLISECONDS);
    assertThat("no consume stream value", expected.size(), is(0));
  }
}
