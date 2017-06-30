package com.xiaoxin.im.ui.appstore.impl;

import android.content.Context;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.xiaoxin.im.utils.AppUpdateUtils;
import com.xiaoxin.im.utils.ToastUtils;

public class MySchedulerListener extends Aria.DownloadSchedulerListener {
  private  String name;
  private  Context mContext;

  public MySchedulerListener(String mName,Context mContext) {
    this.name=mName;
    this.mContext=mContext;
  }

  @Override public void onTaskFail(DownloadTask task) {
      super.onTaskFail(task);
      ToastUtils.showShortToast("下载失败");
    }

    @Override public void onTaskPre(DownloadTask task) {
      super.onTaskPre(task);
    }

    @Override public void onTaskStop(DownloadTask task) {
      super.onTaskStop(task);
    }

    @Override public void onTaskCancel(DownloadTask task) {
      super.onTaskCancel(task);
    }

    @Override public void onTaskRunning(DownloadTask task) {
      super.onTaskRunning(task);
    }

    @Override public void onTaskComplete(DownloadTask task) {
      super.onTaskComplete(task);
      AppUpdateUtils.unZip(name,mContext);
      ToastUtils.showShortToast("下载成功");

    }
  }