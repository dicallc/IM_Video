package com.xiaoxin.im.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.tencent.imsdk.TIMFriendshipManager;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMUserProfile;
import com.tencent.imsdk.TIMValueCallBack;
import com.tencent.qcloud.presentation.event.MessageEvent;
import com.tencent.qcloud.tlslibrary.service.TlsBusiness;
import com.xiaoxin.im.R;
import com.xiaoxin.im.SplashActivity;
import com.xiaoxin.im.cache.UserDao;
import com.xiaoxin.im.common.Constant;
import com.xiaoxin.im.model.FriendshipInfo;
import com.xiaoxin.im.model.GroupInfo;
import com.xiaoxin.im.model.UserInfo;
import com.xiaoxin.im.ui.appstore.AppStoreFragment;
import com.xiaoxin.im.ui.contact.ContactFragment;
import com.xiaoxin.im.ui.conversation.ConversationFragment;
import com.xiaoxin.im.ui.setting.SettingFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * Tab页主界面
 * 主要初始化tab ，申请权限
 */
@SuppressLint("WrongConstant") public class HomeActivity extends FragmentActivity {
  private static final String TAG = HomeActivity.class.getSimpleName();
  private LayoutInflater layoutInflater;
  private FragmentTabHost mTabHost;
  private final Class fragmentArray[] =
      { ConversationFragment.class, ContactFragment.class, AppStoreFragment.class, SettingFragment.class };
  private int mTitleArray[] =
      { R.string.home_conversation_tab, R.string.home_contact_tab, R.string.home_app, R.string.home_setting_tab };
  private int mImageViewArray[] =
      { R.drawable.tab_conversation, R.drawable.tab_contact,R.drawable.tab_app, R.drawable.tab_setting };
  private String mTextviewArray[] = { "contact", "conversation","app", "setting" };
  private ImageView msgUnread;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    if (requestPermission()) {
      Intent intent = new Intent(HomeActivity.this, SplashActivity.class);
      finish();
      startActivity(intent);
    } else {
      initView();
      initData();
      Toast.makeText(this, getString(
          TIMManager.getInstance().getEnv() == 0 ? R.string.env_normal : R.string.env_test),
          Toast.LENGTH_SHORT).show();
    }
  }

  private void initData() {
    TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
      @Override
      public void onError(int i, String s) {

      }

      @Override
      public void onSuccess(TIMUserProfile profile) {
        Constant.sTIMUserProfile=profile;
        UserDao.saveUserEntry(profile.getIdentifier());
      }
    });

  }

  private void initView() {
    layoutInflater = LayoutInflater.from(this);
    mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
    mTabHost.setup(this, getSupportFragmentManager(), R.id.contentPanel);
    int fragmentCount = fragmentArray.length;
    for (int i = 0; i < fragmentCount; ++i) {
      //为每一个Tab按钮设置图标、文字和内容
      TabHost.TabSpec tabSpec =
          mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
      //将Tab按钮添加进Tab选项卡中
      mTabHost.addTab(tabSpec, fragmentArray[i], null);
      mTabHost.getTabWidget().setDividerDrawable(null);
    }
  }

  private View getTabItemView(int index) {
    View view = layoutInflater.inflate(R.layout.home_tab, null);
    ImageView icon = (ImageView) view.findViewById(R.id.icon);
    icon.setImageResource(mImageViewArray[index]);
    TextView title = (TextView) view.findViewById(R.id.title);
    title.setText(mTitleArray[index]);
    if (index == 0) {
      msgUnread = (ImageView) view.findViewById(R.id.tabUnread);
    }
    return view;
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
  }

  public void logout() {
    TlsBusiness.logout(UserInfo.getInstance().getId());
    UserInfo.getInstance().setId(null);
    MessageEvent.getInstance().clear();
    FriendshipInfo.getInstance().clear();
    GroupInfo.getInstance().clear();
    Intent intent = new Intent(HomeActivity.this, SplashActivity.class);
    finish();
    startActivity(intent);
  }

  /**
   * 设置未读tab显示
   */
  public void setMsgUnread(boolean noUnread) {
    msgUnread.setVisibility(noUnread ? View.GONE : View.VISIBLE);
  }

  private boolean requestPermission() {
    if (afterM()) {
      final List<String> permissionsList = new ArrayList<>();
      if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) || (checkSelfPermission(
          Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
        return true;
      }
    }
    return false;
  }

  private boolean afterM() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
  }
}
