package com.xiaoxin.im.ui.gank;

import android.os.Bundle;
import com.xiaoxin.im.R;
import org.apache.cordova.CordovaActivity;

/**
 * Created by Administrator on 2017/6/15 0015.
 */

public class GankActivity extends CordovaActivity{
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    setTheme(R.style.wihte_background);
    super.onCreate(savedInstanceState);

    // enable Cordova apps to be started in the background
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
      moveTaskToBack(true);
    }

    // Set by <content src="index.html" /> in config.xml
    loadUrl(launchUrl);
  }
}
