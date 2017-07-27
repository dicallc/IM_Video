package com.xiaoxin.sleep.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.socks.library.KLog;
import com.xiaoxin.sleep.AppDao;
import com.xiaoxin.sleep.common.Constant;

public class ScreenBroadcastReceiver extends BroadcastReceiver {
  private String action = null;

  @Override public void onReceive(Context context, Intent intent) {
    action = intent.getAction();
    if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
      if (Constant.isOpenScreenSL) {

        //KLog.e("开屏");
      }
    } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
      if (Constant.isOpenScreenSL) {
        AppDao.getInstance().DoScreenBrSync(context);
        KLog.e("锁屏");
      }
    }
  }
}