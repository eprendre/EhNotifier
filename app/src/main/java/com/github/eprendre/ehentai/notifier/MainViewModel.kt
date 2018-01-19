package com.github.eprendre.ehentai.notifier

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.github.eprendre.ehentai.notifier.db.Gallery
import com.github.eprendre.ehentai.notifier.utils.SingleLiveEvent
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by eprendre on 19/01/2018.
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {
  private val disposable = CompositeDisposable()
  val galleries by lazy { App.db.galleryDao().loadGalleries() }
  val itemClickCommand = SingleLiveEvent<Gallery>()

  override fun onCleared() {
    disposable.clear()
  }
}