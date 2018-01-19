package com.github.eprendre.ehentai.notifier.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import android.support.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by eprendre on 17/01/2018.
 */
@Dao
interface GalleryDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertGalleries(galleries: List<Gallery>)

  @Query("select * from galleries order by publishDate DESC limit 200")
  fun loadGalleries(): LiveData<List<Gallery>>
}

@Keep
@Entity(tableName = "galleries", indices = [Index(value = ["name"], unique = true)])
data class Gallery(
    @SerializedName("category") var category: String,
    @SerializedName("publish_date") var publishDate: Long,
    @SerializedName("preview_img") var previewImg: String,
    @SerializedName("name") var name: String,
    @SerializedName("link") var link: String,
    @SerializedName("rating") var rating: Double,
    @SerializedName("uploader") var uploader: String
) {
  @PrimaryKey(autoGenerate = true)
  var id: Int? = null
}
