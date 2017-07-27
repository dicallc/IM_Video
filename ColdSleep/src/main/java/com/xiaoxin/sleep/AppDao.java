package com.xiaoxin.sleep;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.socks.library.KLog;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.library.utils.SpUtils;
import com.xiaoxin.sleep.model.AppCache;
import com.xiaoxin.sleep.model.Event;
import com.xiaoxin.sleep.utils.Utils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

  public void SyncDisData(Activity mActivity) {
    new SyncDisThread(mActivity).start();
  }

  class SyncDisThread extends Thread {
    public Activity mContext;

    public SyncDisThread(Activity mContext) {
      this.mContext = mContext;
    }

    @Override public void run() {
      if (mAllUserAppInfos.size() == 0) mAllUserAppInfos = Utils.getAllUserAppInfos(mContext);
      loadEnAppList();
      List<AppInfo> userSaveDisAppFromDB = getUserSaveDisAppFromDB();
      //从未睡眠列表中寻找用户需要睡眠是否有已经苏醒
      for (AppInfo userdis : userSaveDisAppFromDB) {
        for (AppInfo info : EnList) {
          if (info.appName.equals(userdis.appName)) {
            userdis.isWarn = true;
            break;
          }
        }
      }
      saveUserSaveDisAppToDB(userSaveDisAppFromDB);
      EventBus.getDefault().post(new Event(Event.getDisList, userSaveDisAppFromDB));
    }
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
      loadEnAppList();
      //获取禁用列表
      loadDisAppList();
      saveLocalCache();
      list.clear();
      list.addAll(mAllUserAppInfos);
      if (isSendEvent) EventBus.getDefault().post(new Event(Event.MONDAY));
    }
  }

  private void loadEnAppList() {

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
    KLog.e(EnList);
  }
  private List<String> loadEnAppListApplyPackage() {

    ShellUtils.CommandResult allEnAppsRes =
        ShellUtils.execCommand(LibraryCons.allEnablePackageV3, true, true);
    String mAllEnMsg = allEnAppsRes.successMsg;
    String[] mSplit = mAllEnMsg.split("package:");
    List<String> arr = Arrays.asList(mSplit);
    arr.remove(0);
    return arr;
  }

  private void loadDisAppList() {
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

  private AppInfo findModel(List<AppInfo> lists, AppInfo mAppInfo) {
    AppInfo mPartModel = null;
    for (AppInfo info : lists) {
      if (info.appName.equals(mAppInfo.appName)) {
        mPartModel = info;
        break;
      }
    }
    return mPartModel;
  }

  public List<AppInfo> sortAppList(List<AppInfo> list) {
    Collections.sort(list);
    List<AppInfo> appInfos = list.subList(0, 8);
    return appInfos;
  }

  public int findWarnApp(List<AppInfo> mData) {
    int num = 0;
    for (AppInfo mAppInfo : mData) {
      if (mAppInfo.isWarn) {
        num++;
        ShellUtils.execCommand(LibraryCons.make_app_to_disenble + mAppInfo.packageName, true, true);
        mAppInfo.isWarn = false;
      }
    }
    return num;
  }

  public void DoScreenBrSync(Context mContext) {
    new ScreenBrSyncThread(mContext).start();
  }

  class ScreenBrSyncThread extends Thread {
    private Context mContext;

    public ScreenBrSyncThread(Context mContext) {
      this.mContext = mContext;
    }

    @Override public void run() {
      super.run();
      long startTime = System.nanoTime();  //開始時間
      List<String> mList = loadEnAppListApplyPackage();
      //从缓存中拿到用户保存的需要睡眠的列表
      List<AppInfo> mUserSaveDisAppFromDB = getUserSaveDisAppFromDB();
      //从未睡眠列表中寻找用户需要睡眠是否有已经苏醒
      for (AppInfo userdis : mUserSaveDisAppFromDB) {
        for (String info : mList) {
          if (info.equals(userdis.packageName)) {
            userdis.isWarn = false;
            ShellUtils.execCommand(LibraryCons.make_app_to_disenble + userdis.packageName, true, true);
            break;
          }
        }
      }
      long consumingTime = System.nanoTime() - startTime; //消耗時間
      KLog.e(consumingTime / 1000 + "微妙");
    }
  }
}
