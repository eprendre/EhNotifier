package com.github.eprendre.ehentai.notifier.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Created by eprendre on 17/01/2018.
 */
@Database(entities = [Gallery::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
  abstract fun galleryDao(): GalleryDao
}