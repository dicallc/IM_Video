package com.xiaoxin.sleep.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import com.socks.library.KLog;
import com.xiaoxin.sleep.receiver.WakeReceiver;
import com.xiaoxin.sleep.utils.ScreenBroadcastListener;
import com.xiaoxin.sleep.utils.ScreenManager;

/**
 * 灰色保活手法创建的Service进程
 *
 * @author cat proc/2372/oom_adj
 * @since 2016-04-12
 */
public class StopAppService extends Service {

  private final static String TAG = "dicallc";
  /**
   * 定时唤醒的时间间隔，5分钟
   */
  private final static int ALARM_INTERVAL = 5 * 60 * 1000;
  private final static int WAKE_REQUEST_CODE = 6666;
  private int JOB_ID = 1;

  public static void toLiveService(Context pContext) {
    Intent intent = new Intent(pContext, StopAppService.class);
    pContext.startService(intent);
  }

  private boolean mIsAddAliveAlarm = false;

  @Override public void onCreate() {
    Log.i(TAG, "StopAppService->onCreate");
    if (!mIsAddAliveAlarm) {
      addAliveAlarm();
      mIsAddAliveAlarm = true;
    }
    super.onCreate();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "StopAppService->onStartCommand");
    if (!mIsAddAliveAlarm) {
      addAliveAlarm();
      mIsAddAliveAlarm = true;
    }
    //屏幕关闭的时候启动一个1像素的Activity，开屏的时候关闭Activity
    final ScreenManager screenManager = ScreenManager.getInstance(this);
    ScreenBroadcastListener listener = new ScreenBroadcastListener(this);
    listener.registerListener(new ScreenBroadcastListener.ScreenStateListener() {
      @Override public void onScreenOn() {
        KLog.e("开屏");
        screenManager.finishActivity();
      }

      @Override public void onScreenOff() {
        KLog.e("锁屏" + "screenManager" + screenManager);
        screenManager.startActivity();
      }
    });
    //发送唤醒广播来促使挂掉的UI进程重新启动起来
    //AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    //Intent alarmIntent = new Intent();
    //alarmIntent.setAction(WakeReceiver.GRAY_WAKE_ACTION);
    //PendingIntent operation = PendingIntent.getBroadcast(this, WAKE_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ALARM_INTERVAL, operation);
    return START_REDELIVER_INTENT;
  }

  private void addAliveAlarm() {
    AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    Intent alarmIntent = new Intent();
    alarmIntent.setAction(WakeReceiver.GRAY_WAKE_ACTION);
    PendingIntent operation = PendingIntent.getBroadcast(this, WAKE_REQUEST_CODE, alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      JobScheduler mJobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
      JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,
          new ComponentName(getPackageName(), MyJobService.class.getName()));
      builder.setPeriodic(60 * 1000); //每隔60秒运行一次
      builder.setRequiresCharging(true);
      builder.setPersisted(true);  //设置设备重启后，是否重新执行任务
      builder.setRequiresDeviceIdle(true);
      if (mJobScheduler.schedule(builder.build()) <= 0) {
        //If something goes wrong
      }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), operation);
    } else {
      am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), operation);
    }
  }

  @Override public IBinder onBind(Intent intent) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override public void onDestroy() {
    Log.i(TAG, "StopAppService->onDestroy");
    super.onDestroy();
  }
}
