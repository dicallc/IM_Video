package com.xiaoxin.im.ui.appstore.dao;

import com.socks.library.KLog;
import com.xiaoxin.im.common.Constant;
import com.xiaoxin.im.common.onConnectionFinishLinstener;
import com.xiaoxin.im.model.AppStoreModel;
import com.xiaoxin.im.model.CommonModel;
import com.xiaoxin.im.model.CustomAppBean;
import com.xiaoxin.im.model.QiNiuTokie;
import com.xiaoxin.im.ui.appstore.impl.CommonImpl;
import com.xiaoxin.im.ui.appstore.impl.LoadAppListImpl;
import com.xiaoxin.im.ui.appstore.impl.LoadCustomAppListImpl;
import com.xiaoxin.im.ui.appstore.impl.LoadTokieImpl;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public class AppStoreDao {
  /**
   * 获取app列表
   */
  public static void getAppList(final onConnectionFinishLinstener mLinstener) {
    OkHttpUtils.get()//
        .url(Constant.app_list)//
        .build().execute(new LoadAppListImpl() {
      @Override public void onError(Call call, Exception e, int id) {
        mLinstener.onFail(404, e.getMessage());
      }

      @Override public void onResponse(AppStoreModel response, int id) {

        mLinstener.onSuccess(110, response);
      }
    });
  }

  /**
   * 获取上传图片的tokie
   */
  public static void loadUpdateTokie(final onConnectionFinishLinstener mLinstener) {
    OkHttpUtils.get()//
        .url(Constant.update_tokie)//
        .build().execute(new LoadTokieImpl() {

      @Override public void onError(Call call, Exception e, int id) {
        mLinstener.onFail(404, e.getMessage());
      }

      @Override public void onResponse(QiNiuTokie response, int id) {
        mLinstener.onSuccess(110, response);
      }
    });
  }

  public static void UpdateApp(final onConnectionFinishLinstener mLinstener, String app_ame,
      String key, String version_des, String down_path) {
    OkHttpUtils.post()//
        .url(Constant.update_app_model)//
        .addParams(Constant.platform, "android")
        .addParams(Constant.app_ame, app_ame)
        .addParams(Constant.img, key)
        .addParams(Constant.version_des, version_des)
        .addParams(Constant.down_path, down_path)
        .addParams(Constant.username, Constant.getNikeName())
        .build()
        .execute(new CommonImpl() {

          @Override public void onError(Call call, Exception e, int id) {
            mLinstener.onFail(404, e.getMessage());
          }

          @Override public void onResponse(CommonModel response, int id) {
            mLinstener.onSuccess(110, response);
          }
        });
  }

  public static void loadCustomAppList(final onConnectionFinishLinstener mLinstener) {
    KLog.e( Constant.getNikeName());
    OkHttpUtils.get()//
        .url(Constant.custom_app_list)//
        .addParams("username", Constant.getNikeName())
        .build()
        .execute(new LoadCustomAppListImpl() {

          @Override public void onError(Call call, Exception e, int id) {
            mLinstener.onFail(404, e.getMessage());
          }

          @Override public void onResponse(CustomAppBean response, int id) {
            mLinstener.onSuccess(110, response);
          }
        });
  }
  public static void deleteApp(final onConnectionFinishLinstener mLinstener,String app_name,String img) {
    OkHttpUtils.get()//
        .url(Constant.delete_custom_app)//
        .addParams("username", Constant.getNikeName())
        .addParams("app_ame", app_name)
        .addParams("img", img)
        .build()
        .execute(new CommonImpl() {

          @Override public void onError(Call call, Exception e, int id) {
            mLinstener.onFail(404, e.getMessage());
          }

          @Override public void onResponse(CommonModel response, int id) {
            mLinstener.onSuccess(110, response);
          }
        });
  }
}
