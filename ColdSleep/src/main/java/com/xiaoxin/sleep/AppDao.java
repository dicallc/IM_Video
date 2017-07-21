package com.xiaoxin.sleep;

import android.app.Activity;
import com.google.gson.Gson;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.library.utils.SpUtils;
import com.xiaoxin.library.utils.Utils;
import com.xiaoxin.sleep.model.AppCache;
import com.xiaoxin.sleep.model.Event;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * dao层，与数据打交道，所有数据应从此取，存到此
 */
public class AppDao {

  private AppDao() {
  }

  public static synchronized AppDao getInstance() {
    return SingleHolder.dao;
  }

  private static class SingleHolder {
    private static final AppDao dao = new AppDao();
  }

  List<AppInfo> list = new ArrayList<>();
  List<AppInfo> EnList = new ArrayList<>();
  List<AppInfo> Dislist = new ArrayList<>();
  private List<AppInfo> mAllUserAppInfos = new ArrayList<>();

  public List<AppInfo> getAllUserAppList() {
    return mAllUserAppInfos;
  }

  public void setAllUserAppInfos(List<AppInfo> mAllUserAppInfos) {
    this.mAllUserAppInfos = mAllUserAppInfos;
  }

  private void clearCache() {
    //    list.clear();
    EnList.clear();
    Dislist.clear();
    mAllUserAppInfos.clear();
  }

  public void initListData(Activity mActivity) {
    new SyncThread(mActivity).start();
  }

  class SyncThread extends Thread {
    //private final onCommonLinstener mLinstener;
    public Activity mContext;

    public SyncThread(Activity mContext) {
      this.mContext = mContext;
      //this.mLinstener=mLinstener;
    }

    @Override public void run() {
      clearCache();
      long start = System.currentTimeMillis();
      mAllUserAppInfos = Utils.getAllUserAppInfos(mContext);
      long end = System.currentTimeMillis();
      //获取未被禁用的app列表
      ShellUtils.CommandResult allEnAppsRes =
          ShellUtils.execCommand(LibraryCons.allEnablePackageV3, true, true);
      String mAllEnMsg = allEnAppsRes.successMsg;
      String[] mSplit = mAllEnMsg.split("package:");
      ShellUtils.CommandResult allDisAppsRes =
          ShellUtils.execCommand(LibraryCons.allDisabledPackage, true, true);
      String[] DisApps = allDisAppsRes.successMsg.split("package:");
      for (int i = 1; i < mSplit.length; i++) {
        for (AppInfo mAppInfo : mAllUserAppInfos) {
          if (mSplit[i].equals(mAppInfo.packageName)) {
            mAppInfo.isSelect = false;
            mAppInfo.isEnable = true;
            EnList.add(mAppInfo);
            //看缓存列表是否存在不存在就添加
            checkCacheExit(mAppInfo);
          }
        }
      }
      for (int i = 1; i < DisApps.length; i++) {
        for (AppInfo mAppInfo : mAllUserAppInfos) {
          if (DisApps[i].equals(mAppInfo.packageName)) {
            mAppInfo.isSelect = true;
            mAppInfo.isEnable = false;
            Dislist.add(mAppInfo);
          }
        }
      }
      saveLocalCache();
      if (0 == list.size()) {
        list.addAll(EnList);
      }
      EventBus.getDefault().post(new Event(Event.MONDAY));
    }
  }
  private void checkCacheExit(AppInfo mAppInfo) {
    int index = 0;
    for (int j = 0; j < list.size(); j++) {
      AppInfo model = list.get(j);
      if (model.appName.equals(mAppInfo.appName)) index++;
      if (j == list.size() - 1 && index == 0) {
        list.add(mAppInfo);
      }
    }
  }
  private void saveLocalCache() {
    AppCache mAppCache = new AppCache(EnList, Dislist, mAllUserAppInfos);
    String mJson = new Gson().toJson(mAppCache);
    SpUtils.setParam(App.getAppContext(), LibraryCons.LOCAL_DB_NAME, mJson);
  }
  }
