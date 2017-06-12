package com.github.satoshun.io.reactivex.keeporder;

import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;

public interface KeepOrderTransformer<T> extends
    ObservableTransformer<T, T>,
    SingleTransformer<T, T> {
}
