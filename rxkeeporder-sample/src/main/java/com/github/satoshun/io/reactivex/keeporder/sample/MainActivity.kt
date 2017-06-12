package com.github.satoshun.io.reactivex.keeporder.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.LinearLayout
import android.widget.TextView
import com.github.satoshun.io.reactivex.keeporder.RxKeepOrder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

  private val container: LinearLayout by lazy {
    findViewById(R.id.container) as LinearLayout
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_act)

    loadByKeepOrder()
  }

  private fun loadByKeepOrder() {
    val rxKeepOrder = RxKeepOrder()

    Observable.just("1", "2")
        .delay(2, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(rxKeepOrder.apply())
        .subscribe { addView(it) }

    Observable.just("3", "4", "5")
        .delay(1, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(rxKeepOrder.apply())
        .subscribe { addView(it) }

    Observable.just(6)
        .delay(500, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(rxKeepOrder.apply())
        .subscribe { addView(it.toString()) }
  }

  private fun addView(text: String) {
    val textView = TextView(this)
    textView.text = text
    container.addView(textView)
  }
}
