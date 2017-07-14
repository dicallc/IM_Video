package com.xiaoxin.library.model;

import android.graphics.drawable.Drawable;

/**
 * Created by Administrator on 2017/7/12 0012.
 */

public class AppInfo {
  public String appName;
  public String packageName;
  public Drawable drawable;
  public boolean isSelect;

  public AppInfo() {
  }

  public AppInfo(String mAppName) {
    appName = mAppName;
  }

  public AppInfo(String mAppName, String mPackageName) {
    appName = mAppName;
    packageName = mPackageName;
  }

  public AppInfo(String mAppName, String mPackageName, Drawable mDrawable) {
    appName = mAppName;
    packageName = mPackageName;
    drawable = mDrawable;
  }
}
