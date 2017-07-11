package com.xiaoxin.im.ui.appstore.impl;

import com.google.gson.Gson;
import com.xiaoxin.im.model.CommonModel;
import com.zhy.http.okhttp.callback.Callback;
import okhttp3.Response;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public abstract class CommonImpl extends Callback<CommonModel> {

    @Override public CommonModel parseNetworkResponse(Response response, int id) throws Exception {
        String json = response.body().string();

        CommonModel mModel = new Gson().fromJson(json, CommonModel.class);
        return mModel;
    }
}
