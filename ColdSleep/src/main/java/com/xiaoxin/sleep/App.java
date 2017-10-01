package com.xiaoxin.sleep;

import android.app.Application;
import android.content.Context;
import com.tencent.bugly.crashreport.CrashReport;
import com.xdandroid.hellodaemon.DaemonEnv;
import com.xiaoxin.sleep.service.TraceServiceImpl;

/**
 * Created by Administrator on 2017/7/18 0018.
 */

public class App extends Application {
  static Context context;
  //private static DaoSession daoSession;

  @Override public void onCreate() {
    super.onCreate();
    //配置数据库
    DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
    context = this;
    new Thread(mRunnable).start();
  }

  Runnable mRunnable = new Runnable() {
    @Override public void run() {
      CrashReport.initCrashReport(getApplicationContext(), "2d2fba589f", false);
    }
  };

  public static Context getAppContext() {
    return context;
  }

}
