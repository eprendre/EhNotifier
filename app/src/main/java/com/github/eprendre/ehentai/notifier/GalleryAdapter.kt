package com.github.eprendre.ehentai.notifier

import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.github.eprendre.ehentai.notifier.databinding.GalleryListItemBinding
import com.github.eprendre.ehentai.notifier.db.Gallery
import com.github.eprendre.ehentai.notifier.utils.date
import com.github.eprendre.ehentai.notifier.utils.getLayoutInflater

/**
 * Created by eprendre on 19/01/2018.
 */
class GalleryAdapter(var items: List<Gallery>, val viewModel: MainViewModel) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
  init {
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long {
    return items[position].id!!.toLong()
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val inflater = parent.getLayoutInflater()
    return ViewHolder(DataBindingUtil.inflate(inflater, R.layout.gallery_list_item, parent, false))
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
    holder?.apply {
      val item = items[position]
      binding.item = item
      binding.handler = GalleryAdapterHandler(item, viewModel)
      binding.executePendingBindings()
    }
  }

  inner class ViewHolder(val binding: GalleryListItemBinding) : RecyclerView.ViewHolder(binding.root)
}

class GalleryAdapterHandler(private val item: Gallery, private val viewModel: MainViewModel) {
  val categoryColor = ObservableInt()
  val date = ObservableField<String>()

  init {
    date.set((item.publishDate * 1000).date("yyyy-MM-dd HH:mm"))
    when (item.category) {
      Category.COSPLAY -> categoryColor.set(R.color.colorCOSPLAY)
      Category.MANGA -> categoryColor.set(R.color.colorMANGA)
      Category.DOUJINSHI -> categoryColor.set(R.color.colorDOUJINSHI)
      Category.NONH -> categoryColor.set(R.color.colorNONH)
      Category.MISC -> categoryColor.set(R.color.colorMISC)
      else -> categoryColor.set(android.R.color.black)
    }
  }

  fun onItemClick() {
    viewModel.itemClickCommand.value = item
  }
}
