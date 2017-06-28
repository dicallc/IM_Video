package com.xiaoxin.im.ui.appstore.dao;

import com.xiaoxin.im.common.Constant;
import com.xiaoxin.im.common.onConnectionFinishLinstener;
import com.xiaoxin.im.model.AppStoreModel;
import com.xiaoxin.im.ui.appstore.impl.LoadAppListImpl;
import com.zhy.http.okhttp.OkHttpUtils;

import okhttp3.Call;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public class AppStoreDao {
    public static void getAppList(final onConnectionFinishLinstener mLinstener) {
        OkHttpUtils.get()//
                .url(Constant.app_list)//
                .build().execute(new LoadAppListImpl() {
            @Override
            public void onError(Call call, Exception e, int id) {
                mLinstener.onFail(404, e.getMessage());
            }

            @Override
            public void onResponse(AppStoreModel response, int id) {

                mLinstener.onSuccess(110, response);
            }
        });
    }
}
