package com.xiaoxin.sleep.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import com.xiaoxin.sleep.receiver.ScreenBroadcastReceiver;
import com.xiaoxin.sleep.receiver.WakeReceiver;
import com.xiaoxin.sleep.utils.AlarmManagerCompat;

/**
 * 灰色保活手法创建的Service进程
 * http://www.cnblogs.com/happyhacking/p/5397391.html
 * 命令 http://blog.csdn.net/kc58236582/article/details/50554174
 *
 * @author cat proc/2372/oom_adj
 * @since 2016-04-12
 */
public class StopAppService extends Service {

  private final static String TAG = "dicallc";
  /**
   * 定时唤醒的时间间隔，5分钟
   */
  private final static int ALARM_INTERVAL = 1 * 60 * 1000;
  private final static int WAKE_REQUEST_CODE = 6666;
  private int JOB_ID = 1;

  public static void toLiveService(Context pContext) {
    Intent intent = new Intent(pContext, StopAppService.class);
    pContext.startService(intent);
  }

  private boolean mIsAddAliveAlarm = false;

  @Override public void onCreate() {
    Log.i(TAG, "StopAppService->onCreate");
    //if (!mIsAddAliveAlarm) {
    //    addAliveAlarm();
    //    mIsAddAliveAlarm = true;
    //    registerBroadCast();
    //}
    super.onCreate();
  }

  private void registerBroadCast() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SCREEN_ON);
    filter.addAction(Intent.ACTION_SCREEN_OFF);
    registerReceiver(new ScreenBroadcastReceiver(), filter);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "StopAppService->onStartCommand");
    addAliveAlarm();
    registerBroadCast();
    return START_REDELIVER_INTENT;
  }

  private void addAliveAlarm() {
    int offset = 10 * 1000;//间隔时间10sr
    long triggerAtTime = SystemClock.elapsedRealtime() + offset;
    Intent alarmIntent = new Intent(this, WakeReceiver.class);
    alarmIntent.setAction(WakeReceiver.GRAY_WAKE_ACTION);
    PendingIntent operation = PendingIntent.getBroadcast(this, WAKE_REQUEST_CODE, alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManagerCompat.from(this)
        .setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime, operation);
  }

  @Override public IBinder onBind(Intent intent) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override public void onDestroy() {
    Log.i(TAG, "StopAppService->onDestroy");
    super.onDestroy();
  }
}
