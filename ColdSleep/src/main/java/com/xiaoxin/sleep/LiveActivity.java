package com.xiaoxin.sleep;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class LiveActivity extends Activity {

  public static final String TAG = LiveActivity.class.getSimpleName();
  private static LiveActivity instance;

  /**
   * 开启保活页面
   */
  public static void startHooligan() {
    Intent intent = new Intent(App.getAppContext(), LiveActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    App.getAppContext().startActivity(intent);
    Log.e("HooliganActivity", "HooliganActivity is running");
  }

  /**
   * 关闭保活页面
   */
  public static void killHooligan() {

    if (instance != null) {
      instance.finish();
      Log.e("HooliganActivity", "HooliganActivity is killing");
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    instance = this;
    Log.e(TAG, "onCreate");
    setContentView(R.layout.activity_live);
    Window window = getWindow();
    //放在左上角
    window.setGravity(Gravity.START | Gravity.TOP);
    WindowManager.LayoutParams attributes = window.getAttributes();
    //宽高设计为1个像素
    attributes.width = 1;
    attributes.height = 1;
    //起始坐标
    attributes.x = 0;
    attributes.y = 0;
    window.setAttributes(attributes);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    instance = null;
    Log.e(TAG, "onDestroy");
  }
}
