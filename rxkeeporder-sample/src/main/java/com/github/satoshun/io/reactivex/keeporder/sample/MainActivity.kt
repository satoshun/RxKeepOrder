package com.github.satoshun.io.reactivex.keeporder.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.TextView
import com.github.satoshun.io.reactivex.keeporder.RxKeepOrder
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

  private val container: LinearLayout by lazy {
    findViewById<LinearLayout>(R.id.container)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_act)

    loadByKeepOrder()
  }

  private fun loadByKeepOrder() {
    val rxKeepOrder = RxKeepOrder()
    rxKeepOrder.setObserveScheduler(AndroidSchedulers.mainThread())

    Observable.just("1", "2")
        .delay(2, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.newThread())
        .compose(rxKeepOrder.apply())
        .subscribe { addView(it) }

    Observable.just("3", "4", "5")
        .delay(1, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .compose(rxKeepOrder.apply())
        .subscribe { addView(it) }

    Observable.just(6)
        .delay(500, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.computation())
        .compose(rxKeepOrder.apply())
        .subscribe { addView(it.toString()) }

    Flowable.just(7, 8)
        .delay(100, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .compose(rxKeepOrder.apply())
        .subscribe { addView(it.toString()) }

    Flowable.just("9")
        .map { throw RuntimeException("exception $it") }
        .delay(500, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .compose(rxKeepOrder.apply())
        .subscribe({}, { addView(it.message!!) })

    Maybe.just(10)
        .delay(2000, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.computation())
        .compose(rxKeepOrder.apply())
        .subscribe { addView(it.toString()) }

    Single.just("11")
        .subscribeOn(Schedulers.io())
        .compose(rxKeepOrder.apply())
        .subscribe({ addView(it) }, { })

    Completable.complete()
        .subscribeOn(Schedulers.io())
        .compose(rxKeepOrder.apply<Any>())
        .subscribe({ addView("complete 12") }, { })

    Completable.error(RuntimeException("pseudo"))
        .delay(3000, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .compose(rxKeepOrder.apply<Any>())
        .subscribe({ }, { addView("complete exception 13") })
  }

  private fun addView(text: String) {
    val textView = TextView(this)
    textView.text = text
    container.addView(textView)
  }
}
