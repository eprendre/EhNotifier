package com.github.eprendre.ehentai.notifier

import android.app.Application
import android.arch.persistence.room.Room
import com.github.eprendre.ehentai.notifier.db.AppDatabase
import kotlin.concurrent.thread


/**
 * Created by eprendre on 17/01/2018.
 */
class App : Application() {
  override fun onCreate() {
    super.onCreate()

    thread {
      db = Room.databaseBuilder(applicationContext,
          AppDatabase::class.java, "gallery").build()
    }
  }

  companion object {
    lateinit var db: AppDatabase
  }
}