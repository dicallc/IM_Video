package com.xiaoxin.sleep;

import android.app.Activity;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.library.utils.SpUtils;
import com.xiaoxin.library.utils.Utils;
import com.xiaoxin.sleep.model.AppCache;
import com.xiaoxin.sleep.model.Event;
import java.lang.reflect.Type;
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

  public List<AppInfo> getList() {
    return list;
  }

  public List<AppInfo> getAllUserAppList() {
    return mAllUserAppInfos;
  }

  public void setAllUserAppInfos(List<AppInfo> mAllUserAppInfos) {
    this.mAllUserAppInfos = mAllUserAppInfos;
  }

  private void SetMemoryCache(AppCache mAppCache) {
    EnList.addAll(mAppCache.en);
    Dislist.addAll(mAppCache.dis);
    mAllUserAppInfos.addAll(mAppCache.all);
  }

  private void getLocalCache(Activity mActivity) {
    String json = (String) SpUtils.getParam(mActivity, LibraryCons.LOCAL_DB_NAME, "");
    if (!TextUtils.isEmpty(json)) {
      AppCache mAppCache = new Gson().fromJson(json, AppCache.class);
      clearCache();
      SetMemoryCache(mAppCache);
      list.addAll(mAppCache.en);
    } else {
      initListData(mActivity);
    }
  }

  private void clearCache() {
    //    list.clear();
    EnList.clear();
    Dislist.clear();
    mAllUserAppInfos.clear();
  }

  public void initListData(Activity mActivity) {
    String json = (String) SpUtils.getParam(mActivity, LibraryCons.LOCAL_DB_NAME, "");
    if (!TextUtils.isEmpty(json)) {
      AppCache mAppCache = new Gson().fromJson(json, AppCache.class);
      clearCache();
      SetMemoryCache(mAppCache);
      list.addAll(mAppCache.all);
      EventBus.getDefault().post(new Event(Event.MONDAY));
      new SyncThread(mActivity, true).start();
    } else {
      new SyncThread(mActivity, true).start();
    }
  }

  public void SyncData(Activity mActivity) {
    new SyncThread(mActivity, false).start();
  }

  class SyncThread extends Thread {
    private boolean isSendEvent;
    public Activity mContext;

    public SyncThread(Activity mContext, boolean mB) {
      this.mContext = mContext;
      this.isSendEvent = mB;
    }

    @Override public void run() {
      clearCache();
      mAllUserAppInfos = Utils.getAllUserAppInfos(mContext);
      //获取未被禁用的app列表
      ShellUtils.CommandResult allEnAppsRes =
          ShellUtils.execCommand(LibraryCons.allEnablePackageV3, true, true);
      String mAllEnMsg = allEnAppsRes.successMsg;
      String[] mSplit = mAllEnMsg.split("package:");
      for (int i = 1; i < mSplit.length; i++) {
        for (AppInfo mAppInfo : mAllUserAppInfos) {
          if (mSplit[i].equals(mAppInfo.packageName)) {
            //mAppInfo.isSelect = false;
            mAppInfo.isEnable = true;
            EnList.add(mAppInfo);
            //看缓存列表是否存在不存在就添加
            //checkCacheExit(mAppInfo);
          }
        }
      }
      //获取禁用列表
      ShellUtils.CommandResult allDisAppsRes =
          ShellUtils.execCommand(LibraryCons.allDisabledPackage, true, true);
      String[] DisApps = allDisAppsRes.successMsg.split("package:");
      for (int i = 1; i < DisApps.length; i++) {
        for (AppInfo mAppInfo : mAllUserAppInfos) {
          if (DisApps[i].equals(mAppInfo.packageName)) {
            //mAppInfo.isSelect = true;
            mAppInfo.isEnable = false;
            Dislist.add(mAppInfo);
          }
        }
      }
      saveLocalCache();
      for (int i = 0; i < list.size(); i++) {

      }
      //if (0 == list.size()) {
      list.clear();
      list.addAll(mAllUserAppInfos);
      //}
      if (isSendEvent) EventBus.getDefault().post(new Event(Event.MONDAY));
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

  /**
   * 存储用户操作需要冻结app列表数据
   */
  public void saveUserSaveDisAppToDB(List<AppInfo> mAppInfos) {
    new SaveUserDisThread(mAppInfos).start();
  }

  public List<AppInfo> getUserSaveDisAppFromDB() {
    String json =
        (String) SpUtils.getParam(App.getAppContext(), LibraryCons.LOCAL_USER_DISAPP_DB_NAME, "");
    Type type = new TypeToken<List<AppInfo>>() {
    }.getType();
    List<AppInfo> list = new Gson().fromJson(json, type);
    return list;
  }

  class SaveUserDisThread extends Thread {
    List<AppInfo> mAppInfos;

    public SaveUserDisThread(List<AppInfo> list) {
      this.mAppInfos = list;
    }

    @Override public void run() {
      String mJson = new Gson().toJson(mAppInfos);
      SpUtils.setParam(App.getAppContext(), LibraryCons.LOCAL_USER_DISAPP_DB_NAME, mJson);
    }
  }
}
