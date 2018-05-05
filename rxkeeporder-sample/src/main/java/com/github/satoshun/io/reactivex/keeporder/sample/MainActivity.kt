package com.github.satoshun.io.reactivex.keeporder.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.TextView
import com.github.satoshun.io.reactivex.keeporder.RxKeepOrder
import com.github.satoshun.io.reactivex.keeporder.keepOrder
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
  private val disposables = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_act)

    loadByKeepOrder()
  }

  private fun loadByKeepOrder() {
    val seed = RxKeepOrder(AndroidSchedulers.mainThread())

    disposables.add(
        Observable.just("1", "2")
            .delay(2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .keepOrder(seed)
            .subscribe { addView(it) }
    )

    disposables.add(
        Observable.just("3", "4", "5")
            .delay(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .keepOrder(seed)
            .subscribe { addView(it) }
    )

    disposables.add(
        Observable.just(6)
            .delay(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .keepOrder(seed)
            .subscribe { addView(it.toString()) }
    )

    disposables.add(
        Flowable.just(7, 8)
            .delay(100, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .keepOrder(seed)
            .subscribe { addView(it.toString()) }
    )

    disposables.add(
        Flowable.just("9")
            .map { throw RuntimeException("exception $it") }
            .delay(500, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .keepOrder(seed)
            .subscribe({}, { addView(it.message!!) })
    )

    disposables.add(
        Maybe.just(10)
            .delay(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .keepOrder(seed)
            .subscribe { addView(it.toString()) }
    )

    disposables.add(
        Single.just("11")
            .subscribeOn(Schedulers.io())
            .keepOrder(seed)
            .subscribe({ addView(it) }, { })
    )

    disposables.add(
        Completable.complete()
            .subscribeOn(Schedulers.io())
            .keepOrder(seed)
            .subscribe({ addView("complete 12") }, { })
    )

    disposables.add(
        Completable.error(RuntimeException("pseudo"))
            .delay(3000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .keepOrder(seed)
            .subscribe({ }, { addView("complete exception 13") })
    )
  }

  private fun addView(text: String) {
    val textView = TextView(this)
    textView.text = text
    findViewById<LinearLayout>(R.id.container).addView(textView)
  }

  override fun onDestroy() {
    disposables.dispose()
    super.onDestroy()
  }
}
