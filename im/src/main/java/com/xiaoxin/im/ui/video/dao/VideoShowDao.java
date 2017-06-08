package com.xiaoxin.im.ui.video.dao;

import com.xiaoxin.im.common.Constant;
import com.xiaoxin.im.common.onConnectionFinishLinstener;
import com.xiaoxin.im.model.VideoShowModel;
import com.xiaoxin.im.ui.video.impl.VideoShowCallback;
import com.zhy.http.okhttp.OkHttpUtils;
import okhttp3.Call;

/**
 * Created by dicallc on 2017/6/6 0006.
 */

public class VideoShowDao {
  public static void getVideoShowList(final onConnectionFinishLinstener mLinstener) {
    OkHttpUtils.get()//
        .url(Constant.video_show_url)//
        .build().execute(new VideoShowCallback() {

      @Override public void onError(Call call, Exception e, int id) {
        mLinstener.onFail(404, e.getMessage());
      }

      @Override public void onResponse(VideoShowModel response, int id) {
        mLinstener.onSuccess(110, response);
      }
    });
  }
}
