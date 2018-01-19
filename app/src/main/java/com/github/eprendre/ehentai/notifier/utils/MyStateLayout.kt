package com.github.eprendre.ehentai.notifier.utils

import android.content.Context
import android.util.AttributeSet
import com.github.eprendre.ehentai.notifier.R
import com.github.eprendre.statelayout.StateLayout

/**
 * Created by eprendre on 19/01/2018.
 */
class MyStateLayout(context: Context, attrs: AttributeSet) : StateLayout(context, attrs) {
  override fun getStateLayouts(): List<Int> {
    return listOf(R.layout.state_empty)
  }

  companion object {
    val STATE_EMPTY = R.id.state_empty
    val STATE_CONTENT = R.id.state_content
  }
}