package com.xiaoxin.sleep.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import com.socks.library.KLog;
import com.xdandroid.hellodaemon.AbsWorkService;
import com.xiaoxin.library.utils.SpUtils;
import com.xiaoxin.sleep.AppDao;
import com.xiaoxin.sleep.common.Constant;
import com.xiaoxin.sleep.receiver.ScreenBroadcastReceiver;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import java.util.concurrent.TimeUnit;

/**
 * Android Doze模式调试 http://blog.csdn.net/u010211486/article/details/71082136
 */
public class TraceServiceImpl extends AbsWorkService {

  //是否 任务完成, 不再需要服务运行?
  public static boolean sShouldStopService;
  public static Disposable sDisposable;
  private ScreenBroadcastReceiver mScreenBroadcastReceiver;

  public static void stopService() {
    //我们现在不再需要服务运行了, 将标志位置为 true
    sShouldStopService = true;
    //取消对任务的订阅
    if (sDisposable != null) sDisposable.dispose();
    //取消 Job / Alarm / Subscription
    cancelJobAlarmSub();
  }

  private void registerBroadCast() {
    if (null == mScreenBroadcastReceiver) {
      mScreenBroadcastReceiver = new ScreenBroadcastReceiver();
      IntentFilter filter = new IntentFilter();
      filter.addAction(Intent.ACTION_SCREEN_ON);
      filter.addAction(Intent.ACTION_SCREEN_OFF);
      registerReceiver(mScreenBroadcastReceiver, filter);
    }
  }

  private void unRegisterBroadCast() {
    if (null == mScreenBroadcastReceiver) {
      unregisterReceiver(mScreenBroadcastReceiver);
    }
  }

  /**
   * 是否 任务完成, 不再需要服务运行?
   *
   * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
   */
  @Override public Boolean shouldStopService(Intent intent, int flags, int startId) {
    boolean isopen =
        (boolean) SpUtils.getParam(getApplicationContext(), Constant.ISOPENSCREENSLKEY, false);
    Constant.isOpenScreenSL = isopen;
    registerBroadCast();
    return sShouldStopService;
  }

  @Override public void startWork(Intent intent, int flags, int startId) {
    KLog.d("检查磁盘中是否有上次销毁时保存的数据");
    sDisposable = Flowable.interval(60, TimeUnit.SECONDS)
        //取消任务时取消定时唤醒
        .doOnTerminate(new Action() {
          @Override public void run() throws Exception {
            KLog.d("保存数据到磁盘。");
            cancelJobAlarmSub();
          }
        }).subscribe(new Consumer<Long>() {
          @Override public void accept(Long count) throws Exception {
            KLog.d("每一分钟采集一次数据... count = " + count);
            //如果为零就代表已经被重启过一次，开启睡眠一次
            if (count==0)AppDao.getInstance().DoScreenBrSync(getApplicationContext());
            if (count > 0 && count % 18 == 0) KLog.d("保存数据到磁盘。 saveCount = " + (count / 18 - 1));
          }
        });
  }

  @Override public void stopWork(Intent intent, int flags, int startId) {
    stopService();
  }

  /**
   * 任务是否正在运行?
   *
   * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
   */
  @Override public Boolean isWorkRunning(Intent intent, int flags, int startId) {
    //若还没有取消订阅, 就说明任务仍在运行.
    return sDisposable != null && !sDisposable.isDisposed();
  }

  @Override public IBinder onBind(Intent intent, Void v) {
    return null;
  }

  @Override public void onServiceKilled(Intent rootIntent) {
    KLog.d("保存数据到磁盘(Killed)。");
    unRegisterBroadCast();
  }
}
