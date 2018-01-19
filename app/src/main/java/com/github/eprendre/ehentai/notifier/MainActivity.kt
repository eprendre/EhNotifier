package com.github.eprendre.ehentai.notifier

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.StaggeredGridLayoutManager
import com.baidu.android.pushservice.PushConstants
import com.baidu.android.pushservice.PushManager
import com.github.eprendre.ehentai.notifier.databinding.ActivityMainBinding
import com.github.eprendre.ehentai.notifier.utils.MyStateLayout


class MainActivity : AppCompatActivity() {

  private val binding by lazy { DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main) }
  private val viewModel by lazy { ViewModelProviders.of(this).get(MainViewModel::class.java) }
  private val adapter by lazy { GalleryAdapter(emptyList(), viewModel) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    binding.recyclerView.adapter = adapter
    viewModel.galleries.observe(this, Observer {
      if (it == null) {
        return@Observer
      }
      adapter.items = it
      adapter.notifyDataSetChanged()
      if (it.isEmpty()) {
        binding.stateLayout.displayedChildId = MyStateLayout.STATE_EMPTY
      } else {
        binding.stateLayout.displayedChildId = MyStateLayout.STATE_CONTENT
      }
    })
    viewModel.itemClickCommand.observe(this, Observer {
      if (it == null) {
        return@Observer
      }
      val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it.link))
      startActivity(browserIntent)
    })

    PushManager.startWork(applicationContext, PushConstants.LOGIN_TYPE_API_KEY, "q25FH0G1sfVx39rhBpGLD5tb")
  }
}