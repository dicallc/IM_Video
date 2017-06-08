package com.xiaoxin.im.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

/**
 * Created by Administrator on 2017/6/6 0006.
 */

public class ImageUtils {

  private static RequestBuilder<Drawable> mLoad;

  public static RequestBuilder load(Context ct, String url) {

    mLoad = Glide.with(ct).load(url);
    return mLoad;
  }

  public static RequestBuilder into(ImageView mImageView) {
    mLoad.into(mImageView);
    return mLoad;
  }
}
