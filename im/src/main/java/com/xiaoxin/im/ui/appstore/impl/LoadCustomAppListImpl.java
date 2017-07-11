package com.xiaoxin.im.ui.appstore.impl;

import com.google.gson.Gson;
import com.xiaoxin.im.model.CustomAppBean;
import com.zhy.http.okhttp.callback.Callback;
import okhttp3.Response;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public abstract class LoadCustomAppListImpl extends Callback<CustomAppBean> {

    @Override public CustomAppBean parseNetworkResponse(Response response, int id) throws Exception {
        String json = response.body().string();

        CustomAppBean mVideoShowModel = new Gson().fromJson(json, CustomAppBean.class);
        return mVideoShowModel;
    }
}
