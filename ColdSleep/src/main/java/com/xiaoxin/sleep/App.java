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
  //private static DaoSession daoSession;

  @Override public void onCreate() {
    super.onCreate();
    //配置数据库
    //setupDatabase();
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

  /**
   * 配置数据库
   */
  //private void setupDatabase() {
  //  DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "app_list.db", null);
  //  ////获取可写数据库
  //  SQLiteDatabase db = helper.getWritableDatabase();
  //  ////获取数据库对象
  //  DaoMaster daoMaster = new DaoMaster(db);
  //  ////获取Dao对象管理者
  //  daoSession = daoMaster.newSession();
  //}

  //public static DaoSession getDaoInstant() {
  //  return daoSession;
  ////}
}
