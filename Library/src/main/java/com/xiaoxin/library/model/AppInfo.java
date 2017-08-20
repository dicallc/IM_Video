package com.xiaoxin.library.model;

import android.support.annotation.NonNull;

/**
 * Created by Administrator on 2017/7/12 0012.
 */
public class AppInfo implements Comparable<AppInfo> {
  public String appName;
  public String packageName;
  public String file_path;
  public int open_num;
  public boolean isEnable;
  public boolean isSelect;
  public boolean isWarn;

  public AppInfo(String appName, String packageName, String icon_path) {
    this.appName = appName;
    this.packageName = packageName;
    this.file_path = icon_path;
  }

  public AppInfo(String mAppName) {
    appName = mAppName;
  }

  @Override public int compareTo(@NonNull AppInfo appInfo) {
    if (open_num > appInfo.open_num) {
      return -1;
    } else if(open_num ==appInfo.open_num){
      return 0;
    }else {
      return 1;
    }
  }

  @Override public String toString() {
    return "AppInfo{" + "appName='" + appName + '\'' + ", open_num=" + open_num + '}';
  }

  @Override public boolean equals(Object obj) {
    if (obj instanceof AppInfo) {
      AppInfo u = (AppInfo) obj;
      return this.packageName.equals(u.packageName);
    }else if(obj instanceof String){
      String u = (String) obj;
      return this.packageName.equals(u);
    }
    return super.equals(obj);
  }
}
