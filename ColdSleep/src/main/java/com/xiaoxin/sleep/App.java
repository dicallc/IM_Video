package com.xiaoxin.sleep;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.xdandroid.hellodaemon.DaemonEnv;
import com.xiaoxin.sleep.service.TraceServiceImpl;

/**
 * Created by Administrator on 2017/7/18 0018.
 */

public class App extends Application {
  static Context context;

  @Override public void onCreate() {
    super.onCreate();
    DaemonEnv.initialize(this, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
    try {
      startService(new Intent(this, TraceServiceImpl.class));
    } catch (Exception ignored) {
    }
    context = this;
  }

  public static Context getAppContext() {
    return context;
  }

}
