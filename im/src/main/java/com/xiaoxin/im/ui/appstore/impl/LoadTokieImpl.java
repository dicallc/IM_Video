package com.xiaoxin.im.ui.appstore.impl;

import com.google.gson.Gson;
import com.xiaoxin.im.model.QiNiuTokie;
import com.zhy.http.okhttp.callback.Callback;
import okhttp3.Response;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public abstract class LoadTokieImpl extends Callback<QiNiuTokie> {

    @Override public QiNiuTokie parseNetworkResponse(Response response, int id) throws Exception {
        String json = response.body().string();

        QiNiuTokie mVideoShowModel = new Gson().fromJson(json, QiNiuTokie.class);
        return mVideoShowModel;
    }
}
