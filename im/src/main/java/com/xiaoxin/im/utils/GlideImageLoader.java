package com.xiaoxin.im.utils;

import android.app.Activity;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.lzy.imagepicker.loader.ImageLoader;
import com.xiaoxin.im.R;
import java.io.File;

/**
 * Created by Administrator on 2017/7/7 0007.
 */

public class GlideImageLoader implements ImageLoader {
  @Override
  public void displayImage(Activity mActivity, String path, ImageView imageView, int width, int height) {
    Glide.with(mActivity)//
        .load(Uri.fromFile(new File(path)))//
        .placeholder(R.mipmap.default_image)//
        .error(R.mipmap.default_image)//
        .centerCrop()
        //.resize(width, height)//
        //.memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)//
        .into(imageView);
  }

  @Override public void clearMemoryCache() {

  }
}
