package com.xiaoxin.library.model;

/**
 * Created by Administrator on 2017/7/12 0012.
 */

public class AppInfo {
    public String appName;
    public String packageName;
    public String file_path;
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

}
