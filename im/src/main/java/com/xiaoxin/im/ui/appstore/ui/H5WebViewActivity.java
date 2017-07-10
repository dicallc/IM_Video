package com.xiaoxin.im.ui.appstore.ui;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.xiaoxin.im.R;

public class H5WebViewActivity extends AppCompatActivity {

  @BindView(R.id.webview) WebView mWebview;
  @BindView(R.id.container) FrameLayout mContainer;
  private String mUrl;
  private TransitionFragment mTransitionFragment;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.appstore_h5_web_view);
    ButterKnife.bind(this);
    Intent mIntent = getIntent();
    mUrl = mIntent.getStringExtra("url");
    getWindow().setFormat(PixelFormat.TRANSLUCENT);
    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    showTranstion();
    initView();
  }

  private void showTranstion() {
    mTransitionFragment = TransitionFragment.newInstance("", "");
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.replace(R.id.container,mTransitionFragment ); transaction.commit();
  }

  private void hideTranstion() {
    if (mTransitionFragment == null) {
      return;
    }

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.remove(mTransitionFragment);
    transaction.commit();

    mTransitionFragment = null;
    try {
    mContainer.setVisibility(View.GONE);
    mContainer = null;
    } catch (Throwable ignore) {
    }
  }

  private void initView() {
    mWebview.loadUrl(mUrl);
    WebSettings webSettings = mWebview.getSettings();
    webSettings.setJavaScriptEnabled(true);
    mWebview.setWebViewClient(new WebViewClient() {
      @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
      }
    });
    mWebview.setWebChromeClient(new WebChromeClient(){
      @Override public void onProgressChanged(WebView mWebView, int num) {
        super.onProgressChanged(mWebView, num);
        if (100==num){
          hideTranstion();
        }
      }
    });
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()) {
      mWebview.goBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }
}
