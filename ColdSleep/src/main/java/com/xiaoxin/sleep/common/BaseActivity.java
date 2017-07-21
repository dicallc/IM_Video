package com.xiaoxin.sleep.common;

import android.support.v7.app.AppCompatActivity;
import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Administrator on 2017/7/21 0021.
 */

public class BaseActivity extends AppCompatActivity {
  private MaterialDialog mDialog;

  protected void showloadDialog(String title) {
    mDialog = new MaterialDialog.Builder(this).title(title)
        .content("请深呼吸休息一下")
        .progress(true, 0)
        .progressIndeterminateStyle(true)
        .show();
  }

  protected void goneloadDialog() {
    if (null != mDialog) if (mDialog.isShowing()) mDialog.dismiss();
  }
}
