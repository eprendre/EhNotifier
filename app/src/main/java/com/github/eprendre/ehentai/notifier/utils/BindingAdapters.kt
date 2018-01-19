package com.github.eprendre.ehentai.notifier.utils

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide

/**
 * Created by eprendre on 13/12/2017.
 */

@BindingAdapter(value = ["imageUrl"])
fun loadImage(view: ImageView, url: String) {
  Glide.with(view.context).load(url).into(view)
}