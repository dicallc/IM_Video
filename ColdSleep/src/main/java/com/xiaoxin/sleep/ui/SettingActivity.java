package com.xiaoxin.sleep.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.allen.library.SuperTextView;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.sleep.R;
import com.xiaoxin.sleep.common.Constant;
import com.xiaoxin.sleep.utils.ToastUtils;
import com.xiaoxin.sleep.view.SettingNormalView;

public class SettingActivity extends AppCompatActivity {

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.main_layout) CoordinatorLayout mainLayout;
  @BindView(R.id.setting_select_app) SettingNormalView settingSelectApp;
  @BindView(R.id.setting_screen_sleep) SuperTextView settingScreenSleep;
  @BindView(R.id.setting_screen_sleep_time) SettingNormalView settingScreenSleepTime;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    ButterKnife.bind(this);
    initToobar();
    settingScreenSleep.setSwitchCheckedChangeListener(
        new SuperTextView.OnSwitchCheckedChangeListener() {
          @Override public void onCheckedChanged(CompoundButton mCompoundButton, boolean mB) {
            Constant.isOpenScreenSL = mB;
          }
        });
  }

  private void initToobar() {
    toolbar.setTitle("设置");
    setSupportActionBar(toolbar);
    //关键下面两句话，设置了回退按钮，及点击事件的效果
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        finish();
      }
    });
  }

  @OnClick(R.id.setting_select_app) public void toSelectApp() {
    Intent mIntent = new Intent(SettingActivity.this, SelectAppActivity.class);
    mIntent.putExtra(LibraryCons.ACTION, LibraryCons.ACTION_OPEN);
    startActivity(mIntent);
  }

  @OnClick(R.id.setting_screen_sleep_time) public void showSingleChoice() {
    new MaterialDialog.Builder(this).title("睡眠时间")
        .items(R.array.sleep_time)
        .positiveText("确定")
        .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
          @Override public boolean onSelection(MaterialDialog mMaterialDialog, View mView, int mI,
              CharSequence mCharSequence) {
            ToastUtils.showShortToast(mI + ": " + mCharSequence);
            return true;
          }
        })
        .show();
  }
  @Override
  protected void onResume() {
    super.onResume();
    overridePendingTransition(android.R.anim.fade_in,
        android.R.anim.fade_out);
    super.onResume();
  }
}
