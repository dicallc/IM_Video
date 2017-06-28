package com.xiaoxin.im.ui.appstore.impl;

import com.google.gson.Gson;
import com.xiaoxin.im.model.AppStoreModel;
import com.xiaoxin.im.model.VideoShowModel;
import com.zhy.http.okhttp.callback.Callback;

import okhttp3.Response;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public abstract class LoadAppListImpl extends Callback<AppStoreModel> {

    @Override public AppStoreModel parseNetworkResponse(Response response, int id) throws Exception {
        String json = response.body().string();

        AppStoreModel mVideoShowModel = new Gson().fromJson(json, AppStoreModel.class);
        return mVideoShowModel;
    }
}
