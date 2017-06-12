package com.github.satoshun.io.reactivex.keeporder;

import com.github.satoshun.io.reactivex.keeporder.BuildConfig;
import com.github.satoshun.io.reactivex.keeporder.RxKeepOrder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class RxKeepOrder_MultiTypeTest {

  private RxKeepOrder rxKeepOrder;

  @Before public void setup() throws Exception {
    rxKeepOrder = new RxKeepOrder();
  }

  @Test public void observable_single__keep_order_two() throws Exception {
    final CountDownLatch latch = new CountDownLatch(2);
    final List<Integer> expected = new ArrayList<>(Arrays.asList(
        1,
        2)
    );

    Observable.just(1)
        .delay(100, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) {
            assertThat(expected.remove(0), is(value));
            latch.countDown();
          }
        });
    Single.just(2)
        .delay(50, TimeUnit.MILLISECONDS)
        .compose(rxKeepOrder.<Integer>apply())
        .subscribeOn(Schedulers.io())
        .subscribe(new Consumer<Integer>() {
          @Override public void accept(Integer value) {
            assertThat(expected.remove(0), is(value));
            latch.countDown();
          }
        });
    latch.await(1000, TimeUnit.MILLISECONDS);
    assertThat("no consume stream value", expected.size(), is(0));
  }
}
