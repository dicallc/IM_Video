package com.xiaoxin.library.utils;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import com.xiaoxin.library.model.AppInfo;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/7/12 0012.
 */

public class Utils {
  /**
   * 获取所有用户app信息
   */
  public static  List<AppInfo> getAllUserAppInfos(Activity mContext) {
    List<AppInfo> mAppInfos = new ArrayList<>();
    PackageManager pm = mContext.getApplication().getPackageManager();
    List<PackageInfo> packgeInfos =
        pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        /* 获取应用程序的名称，不是包名，而是清单文件中的labelname
            String str_name = packageInfo.applicationInfo.loadLabel(pm).toString();
            appInfo.setAppName(str_name);
         */
    for (PackageInfo packgeInfo : packgeInfos) {
      ApplicationInfo mApplicationInfo = packgeInfo.applicationInfo;
      //如果是系统程序就跳出本次循环
      if ((mApplicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) continue;
      String appName = packgeInfo.applicationInfo.loadLabel(pm).toString();
      String packageName = packgeInfo.packageName;
      Drawable drawable = packgeInfo.applicationInfo.loadIcon(pm);
      AppInfo appInfo = new AppInfo(appName, packageName, drawable);
      mAppInfos.add(appInfo);
    }
    return mAppInfos;
  }

  /**
   * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
   *
   * @return 应用程序是/否获取Root权限
   */
  public static boolean upgradeRootPermission(String pkgCodePath) {
    Process process = null;
    DataOutputStream os = null;
    try {
      String cmd = "chmod 777 " + pkgCodePath;
      process = Runtime.getRuntime().exec("su"); //切换到root帐号
      os = new DataOutputStream(process.getOutputStream());
      os.writeBytes(cmd + "\n");
      os.writeBytes("exit\n");
      os.flush();
      process.waitFor();
      if (process.waitFor() == 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      return false;
    } finally {
      try {
        if (os != null) {
          os.close();
        }
        process.destroy();
      } catch (Exception e) {
      }
    }
  }
}
