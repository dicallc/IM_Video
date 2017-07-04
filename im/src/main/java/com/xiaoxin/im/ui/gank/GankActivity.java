package com.xiaoxin.im.ui.gank;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import com.xiaoxin.im.R;
import org.apache.cordova.CordovaActivity;

/**
 * Created by Administrator on 2017/6/15 0015.
 */

public class GankActivity extends CordovaActivity{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {

    //showDialog();
    super.onCreate(savedInstanceState);
    // enable Cordova apps to be started in the background
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
      moveTaskToBack(true);
    }
    // Set by <content src="index.html" /> in config.xml
    loadUrl(launchUrl);
  }

  @Override protected View loaddingFinish() {
    return null;
  }

  private void showDialog() {
    final Dialog dialog = new Dialog(GankActivity.this);
    dialog.setContentView(R.layout.activity_gank_layout);
    dialog.show();
  }
}
