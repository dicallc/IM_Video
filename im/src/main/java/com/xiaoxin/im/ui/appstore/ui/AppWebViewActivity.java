package com.xiaoxin.im.ui.appstore.ui;

import android.os.Bundle;
import android.view.View;
import com.socks.library.KLog;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.xiaoxin.im.R;
import org.apache.cordova.CordovaActivity;

public class AppWebViewActivity extends CordovaActivity {
  public String url;
  @Override public void onCreate(Bundle savedInstanceState) {
    String mName = getIntent().getStringExtra("name");
    url="file:///data/user/0/com.xiaoxin.im/files/unzip/" + mName + "/www/index.html";
    setOpenUrl(url);
    super.onCreate(savedInstanceState);
    Bundle extras = getIntent().getExtras();
    if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
      moveTaskToBack(true);
    }

    loadUrl(url);
    View mView = View.inflate(AppWebViewActivity.this, R.layout.loadding_cordova, null);
    appView.showCustomView(mView,
        new IX5WebChromeClient.CustomViewCallback() {
          @Override public void onCustomViewHidden() {

            KLog.e("onCustomViewHidden");
          }
        });
  }

  @Override protected View loaddingFinish() {
    appView.hideCustomView();
    KLog.e("loaddingFinish");
    return null;
  }
}
