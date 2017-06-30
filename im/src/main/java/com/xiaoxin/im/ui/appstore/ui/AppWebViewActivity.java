package com.xiaoxin.im.ui.appstore.ui;

import android.os.Bundle;
import org.apache.cordova.CordovaActivity;

public class AppWebViewActivity extends CordovaActivity {
  public String url;
  @Override public void onCreate(Bundle savedInstanceState) {
    String mName = getIntent().getStringExtra("name");
    url="file:///data/user/0/com.xiaoxin.im/files/unzip/" + mName + "/www/index.html";
    setOpenUrl(url);
    //showDialog();
    super.onCreate(savedInstanceState);
    // enable Cordova apps to be started in the background
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
      moveTaskToBack(true);
    }

    // Set by <content src="index.html" /> in config.xml
    loadUrl(url);
  }
}
