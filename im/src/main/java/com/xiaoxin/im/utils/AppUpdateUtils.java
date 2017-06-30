package com.xiaoxin.im.utils;

import android.content.Context;
import android.util.Log;
import java.io.File;


/**
 * Created by Administrator on 2017/6/29 0029.
 */

public class AppUpdateUtils {
  public static boolean checkFile(Context mContext,String name) {
    File mFile = new File(mContext.getFilesDir() + "/unzip/"+name);
    if (mFile.exists()) {
      return true;
      //ToastUtils.showShortToast("文件已经存在");
    } else {
      return false;
      //ToastUtils.showShortToast("文件不存在");
    }
  }
  public static void unZip(String mName, Context mContext) {
    ZipExtractorTask mZipExtractorTask =
        new ZipExtractorTask(mContext.getFilesDir() + "/"+mName+".zip",
            mContext.getFilesDir() + "/unzip/"+mName+"/", mContext,
            true);
    mZipExtractorTask.execute();
  }
  /**
   * 版本号比较
   * 0代表相等，1数据库版本大，-1代表服务器版本大
   * @param version1 数据库版本
   * @param version2 服务器版本
   * @return
   */
  public static int compareVersion(String version1, String version2) {
    if (version1.equals(version2)) {
      return 0;
    }
    String[] version1Array = version1.split("\\.");
    String[] version2Array = version2.split("\\.");
    Log.d("HomePageActivity", "version1Array=="+version1Array.length);
    Log.d("HomePageActivity", "version2Array=="+version2Array.length);
    int index = 0;
    // 获取最小长度值
    int minLen = Math.min(version1Array.length, version2Array.length);
    int diff = 0;
    // 循环判断每位的大小
    Log.d("HomePageActivity", "verTag2=2222="+version1Array[index]);
    while (index < minLen
        && (diff = Integer.parseInt(version1Array[index])
        - Integer.parseInt(version2Array[index])) == 0) {
      index++;
    }
    if (diff == 0) {
      // 如果位数不一致，比较多余位数
      for (int i = index; i < version1Array.length; i++) {
        if (Integer.parseInt(version1Array[i]) > 0) {
          return 1;
        }
      }

      for (int i = index; i < version2Array.length; i++) {
        if (Integer.parseInt(version2Array[i]) > 0) {
          return -1;
        }
      }
      return 0;
    } else {
      return diff > 0 ? 1 : -1;
    }
  }
}
