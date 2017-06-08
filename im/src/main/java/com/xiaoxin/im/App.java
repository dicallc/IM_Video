package com.xiaoxin.im;

import android.app.Application;
import android.content.Context;
import com.socks.library.KLog;
import com.tencent.imsdk.TIMGroupReceiveMessageOpt;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMOfflinePushListener;
import com.tencent.imsdk.TIMOfflinePushNotification;
import com.tencent.qalsdk.sdk.MsfSdkUtils;
import com.xiaoxin.im.utils.Foreground;

/**
 * Created by Administrator on 2017/5/31 0031.
 */

public class App extends Application {
  private static Context context;

  @Override public void onCreate() {
    super.onCreate();
    KLog.init(BuildConfig.LOG_DEBUG, "dicallc");
    Foreground.init(this);
    context = getApplicationContext();
    if (MsfSdkUtils.isMainProcess(this)) {
      TIMManager.getInstance().setOfflinePushListener(new TIMOfflinePushListener() {
        @Override public void handleNotification(TIMOfflinePushNotification notification) {
          if (notification.getGroupReceiveMsgOpt() == TIMGroupReceiveMessageOpt.ReceiveAndNotify) {
            //消息被设置为需要提醒
            notification.doNotify(getApplicationContext(), R.mipmap.ic_launcher);
          }
        }
      });
    }
  }

  public static Context getContext() {
    return context;
  }
}
