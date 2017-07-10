package com.xiaoxin.im;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.view.CropImageView;
import com.socks.library.KLog;
import com.tencent.bugly.imsdk.crashreport.CrashReport;
import com.tencent.imsdk.TIMGroupReceiveMessageOpt;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMOfflinePushListener;
import com.tencent.imsdk.TIMOfflinePushNotification;
import com.tencent.qalsdk.sdk.MsfSdkUtils;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.tinkerpatch.sdk.TinkerPatch;
import com.tinkerpatch.sdk.loader.TinkerPatchApplicationLike;
import com.xiaoxin.im.ui.appstore.PreLoadX5Service;
import com.xiaoxin.im.utils.Foreground;
import com.xiaoxin.im.utils.GlideImageLoader;

/**
 * Created by Administrator on 2017/5/31 0031.
 */

public class App extends Application {
  private static Context context;
  private ApplicationLike tinkerApplicationLike;

  @Override public void onCreate() {
    super.onCreate();
    initPicker();
    // 在主进程初始化调用哈
    initX5();
    //BlockCanary.install(this, new AppBlockCanaryContext()).start();
    initTinkerPatch();
    CrashReport.initCrashReport(getApplicationContext(), "1400032075", false);
    //useSample();
    KLog.init(BuildConfig.LOG_DEBUG, "dicallc");
    Foreground.init(this);
    context = getApplicationContext();
    if (MsfSdkUtils.isMainProcess(this)) {
      TIMManager.getInstance().setOfflinePushListener(new TIMOfflinePushListener() {
        @Override public void handleNotification(TIMOfflinePushNotification notification) {
          if (notification.getGroupReceiveMsgOpt() == TIMGroupReceiveMessageOpt.ReceiveAndNotify) {
            //消息被设置为需要提醒
            notification.doNotify(getApplicationContext(), R.mipmap.ic_launcher);
          }
        }
      });
    }
  }

  private void initPicker() {
    ImagePicker imagePicker = ImagePicker.getInstance();
    imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
    imagePicker.setShowCamera(true);  //显示拍照按钮
    imagePicker.setMultiMode(false);
    imagePicker.setCrop(true);        //允许裁剪（单选才有效）
    imagePicker.setSaveRectangle(true); //是否按矩形区域保存
    imagePicker.setSelectLimit(1);    //选中数量限制
    imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
    imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
    imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
    //imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
    //imagePicker.setOutPutY(1000);//保存文件的高度。单位像素
  }

  @Override protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(base);
  }

  /**
   * 我们需要确保至少对主进程跟patch进程初始化 TinkerPatch
   */
  private void initTinkerPatch() {
    // 我们可以从这里获得Tinker加载过程的信息
    if (BuildConfig.TINKER_ENABLE) {
      tinkerApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();
      // 初始化TinkerPatch SDK
      TinkerPatch.init(
          tinkerApplicationLike
          //                new TinkerPatch.Builder(tinkerApplicationLike)
          //                    .requestLoader(new OkHttp3Loader())
          //                    .build()
      )
          .reflectPatchLibrary()
          .setPatchRollbackOnScreenOff(true)
          .setPatchRestartOnSrceenOff(true)
          .setFetchPatchIntervalByHours(3)
      ;
      // 获取当前的补丁版本
      KLog.e("Current patch version is " + TinkerPatch.with().getPatchVersion());

      // fetchPatchUpdateAndPollWithInterval 与 fetchPatchUpdate(false)
      // 不同的是，会通过handler的方式去轮询
      TinkerPatch.with().fetchPatchUpdateAndPollWithInterval();
    }
  }
  private void initX5() {
    Intent intent = new Intent(this, PreLoadX5Service.class);
    startService(intent);
  }


  public static Context getContext() {
    return context;
  }
}
