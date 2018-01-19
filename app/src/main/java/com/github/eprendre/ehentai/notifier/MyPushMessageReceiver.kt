package com.github.eprendre.ehentai.notifier

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.baidu.android.pushservice.PushManager
import com.baidu.android.pushservice.PushMessageReceiver
import com.github.eprendre.ehentai.notifier.db.Gallery
import com.github.eprendre.ehentai.notifier.utils.hour
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.toast

/*
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 *onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 *onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调
 * 返回值中的errorCode，解释如下：
 *0 - Success
 *10001 - Network Problem
 *10101  Integrate Check Error
 *30600 - Internal Server Error
 *30601 - Method Not Allowed
 *30602 - Request Params Not Valid
 *30603 - Authentication Failed
 *30604 - Quota Use Up Payment Required
 *30605 -Data Required Not Found
 *30606 - Request Time Expires Timeout
 *30607 - Channel Token Timeout
 *30608 - Bind Relation Not Found
 *30609 - Bind Number Too Many
 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 *
 */

class MyPushMessageReceiver : PushMessageReceiver() {

  /**
   * 调用PushManager.startWork后，sdk将对push
   * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
   * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
   *
   * @param context
   * BroadcastReceiver的执行Context
   * @param errorCode
   * 绑定接口返回值，0 - 成功
   * @param appid
   * 应用id。errorCode非0时为null
   * @param userId
   * 应用user id。errorCode非0时为null
   * @param channelId
   * 应用channel id。errorCode非0时为null
   * @param requestId
   * 向服务端发起的请求id。在追查问题时有用；
   * @return none
   */
  override fun onBind(context: Context, errorCode: Int, appid: String,
                      userId: String, channelId: String, requestId: String) {
    val responseString = ("onBind errorCode=" + errorCode + " appid="
        + appid + " userId=" + userId + " channelId=" + channelId
        + " requestId=" + requestId)
    Log.d(TAG, responseString)

    if (errorCode == 0) {
      // 绑定成功
      Log.d(TAG, "绑定成功")
      PushManager.setTags(context,
          listOf(
              Category.COSPLAY,
              Category.MANGA
//              Category.DOUJINSHI,
//              Category.NONH,
//              Category.IMAGESET,
//              Category.WESTERN,
//              Category.GAMECG,
//              Category.ARTISTCG
          ))
    }
  }

  /**
   * 接收透传消息的函数。
   *
   * @param context
   * 上下文
   * @param message
   * 推送的消息
   * @param customContentString
   * 自定义内容,为空或者json字符串
   */
  override fun onMessage(context: Context, message: String,
                         customContentString: String?) {
    val messageString = ("透传消息 onMessage=\"" + message
        + "\" customContentString=" + customContentString)
    Log.d(TAG, messageString)

    Single
        .create<List<Gallery>> {
          val list = Gson().fromJson<List<Gallery>>(message, object : TypeToken<List<Gallery>>() {}.type)
          it.onSuccess(list)
        }
        .subscribeOn(Schedulers.io())
        .map {
          it.reversed().filter {
            when (it.category) {
              Category.COSPLAY -> return@filter it.rating >= 4
              Category.MANGA -> return@filter it.rating >= 4 && it.name.contains("chinese", ignoreCase = true)
              else -> return@filter false
            }
          }
        }
        .doOnSuccess { App.db.galleryDao().insertGalleries(it) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(onSuccess = {
          if (it.isNotEmpty()) {
            val category = it[0].category
            when (category) {
              Category.COSPLAY -> showNotification(context, it, 10000)
              Category.MANGA -> showNotification(context, it, 10001)
            }
          }
        }, onError = {
          context.toast(it.toString())
        })
  }

  @SuppressLint("NewApi")
  private fun showNotification(context: Context, galleryList: List<Gallery>, notificationId: Int) {
    val isSilent = System.currentTimeMillis().hour().toInt() < 8//silent from 0:00 to 8:00
    val mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val NOTIFICATION_CHANNEL_ID = "my_channel_id_01"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications",
          if (isSilent) NotificationManager.IMPORTANCE_LOW else NotificationManager.IMPORTANCE_DEFAULT)

      notificationChannel.description = "Channel description"
      notificationChannel.enableLights(true)
      notificationChannel.enableVibration(true)
      mNotifyMgr.createNotificationChannel(notificationChannel)
    }

    val resultIntent = Intent(context, MainActivity::class.java)
    val resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentTitle("e-hentai ${galleryList[0].category}")
        .setContentText("${galleryList[0].name} published")
        .setContentIntent(resultPendingIntent)
        .setAutoCancel(true)

    if (isSilent) {
      builder.setDefaults(Notification.DEFAULT_LIGHTS)
    } else {
      builder.setDefaults(Notification.DEFAULT_ALL)
    }

    mNotifyMgr.notify(notificationId, builder.build())
  }

  /**
   * 接收通知到达的函数。
   *
   * @param context
   * 上下文
   * @param title
   * 推送的通知的标题
   * @param description
   * 推送的通知的描述
   * @param customContentString
   * 自定义内容，为空或者json字符串
   */

  override fun onNotificationArrived(context: Context, title: String,
                                     description: String, customContentString: String?) {

    val notifyString = ("通知到达 onNotificationArrived  title=\"" + title
        + "\" description=\"" + description + "\" customContent="
        + customContentString)
    Log.d(TAG, notifyString)
  }

  /**
   * 接收通知点击的函数。
   *
   * @param context
   * 上下文
   * @param title
   * 推送的通知的标题
   * @param description
   * 推送的通知的描述
   * @param customContentString
   * 自定义内容，为空或者json字符串
   */
  override fun onNotificationClicked(context: Context, title: String,
                                     description: String, customContentString: String?) {
    val notifyString = ("通知点击 onNotificationClicked title=\"" + title + "\" description=\""
        + description + "\" customContent=" + customContentString)
    Log.d(TAG, notifyString)
  }

  /**
   * setTags() 的回调函数。
   *
   * @param context
   * 上下文
   * @param errorCode
   * 错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
   * @param successTags
   * 设置成功的tag
   * @param failTags
   * 设置失败的tag
   * @param requestId
   * 分配给对云推送的请求的id
   */
  override fun onSetTags(context: Context, errorCode: Int,
                         successTags: List<String>, failTags: List<String>, requestId: String) {
    val responseString = ("onSetTags errorCode=" + errorCode
        + " sucessTags=" + successTags + " failTags=" + failTags
        + " requestId=" + requestId)
    Log.d(TAG, responseString)

  }

  /**
   * delTags() 的回调函数。
   *
   * @param context
   * 上下文
   * @param errorCode
   * 错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
   * @param successTags
   * 成功删除的tag
   * @param failTags
   * 删除失败的tag
   * @param requestId
   * 分配给对云推送的请求的id
   */
  override fun onDelTags(context: Context, errorCode: Int,
                         successTags: List<String>, failTags: List<String>, requestId: String) {
    val responseString = ("onDelTags errorCode=" + errorCode
        + " sucessTags=" + successTags + " failTags=" + failTags
        + " requestId=" + requestId)
    Log.d(TAG, responseString)

  }

  /**
   * listTags() 的回调函数。
   *
   * @param context
   * 上下文
   * @param errorCode
   * 错误码。0表示列举tag成功；非0表示失败。
   * @param tags
   * 当前应用设置的所有tag。
   * @param requestId
   * 分配给对云推送的请求的id
   */
  override fun onListTags(context: Context, errorCode: Int, tags: List<String>,
                          requestId: String) {
    val responseString = ("onListTags errorCode=" + errorCode + " tags="
        + tags)
    Log.d(TAG, responseString)

  }

  /**
   * PushManager.stopWork() 的回调函数。
   *
   * @param context
   * 上下文
   * @param errorCode
   * 错误码。0表示从云推送解绑定成功；非0表示失败。
   * @param requestId
   * 分配给对云推送的请求的id
   */
  override fun onUnbind(context: Context, errorCode: Int, requestId: String) {
    val responseString = ("onUnbind errorCode=" + errorCode
        + " requestId = " + requestId)
    Log.d(TAG, responseString)

    if (errorCode == 0) {
      // 解绑定成功
      Log.d(TAG, "解绑成功")
    }
  }

  companion object {
    /** TAG to Log  */
    val TAG = MyPushMessageReceiver::class.java.simpleName!!
  }
}
