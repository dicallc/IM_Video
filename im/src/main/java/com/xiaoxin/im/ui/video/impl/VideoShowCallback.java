package com.xiaoxin.im.ui.video.impl;

import com.google.gson.Gson;
import com.xiaoxin.im.model.VideoShowModel;
import com.zhy.http.okhttp.callback.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/6/6 0006.
 */

public abstract class VideoShowCallback extends Callback<VideoShowModel> {
  @Override public VideoShowModel parseNetworkResponse(Response response, int id) throws Exception {
    String json = response.body().string();
    VideoShowModel mVideoShowModel = new Gson().fromJson(json, VideoShowModel.class);
    return mVideoShowModel;
  }


}
