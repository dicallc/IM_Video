package com.xiaoxin.im;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.huawei.android.pushagent.PushManager;
import com.tencent.imsdk.TIMCallBack;
import com.tencent.imsdk.TIMConnListener;
import com.tencent.imsdk.TIMLogLevel;
import com.tencent.imsdk.TIMManager;
import com.tencent.imsdk.TIMUserConfig;
import com.tencent.imsdk.TIMUserStatusListener;
import com.tencent.qcloud.presentation.business.InitBusiness;
import com.tencent.qcloud.presentation.business.LoginBusiness;
import com.tencent.qcloud.presentation.event.FriendshipEvent;
import com.tencent.qcloud.presentation.event.GroupEvent;
import com.tencent.qcloud.presentation.event.MessageEvent;
import com.tencent.qcloud.presentation.event.RefreshEvent;
import com.tencent.qcloud.presentation.presenter.SplashPresenter;
import com.tencent.qcloud.presentation.viewfeatures.SplashView;
import com.tencent.qcloud.tlslibrary.activity.HostLoginActivity;
import com.tencent.qcloud.tlslibrary.service.TLSService;
import com.tencent.qcloud.tlslibrary.service.TlsBusiness;
import com.tencent.qcloud.ui.NotifyDialog;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaoxin.im.model.UserInfo;
import com.xiaoxin.im.ui.HomeActivity;
import com.xiaoxin.im.ui.appstore.ui.H5WebViewActivity;
import com.xiaoxin.im.ui.customview.DialogActivity;
import com.xiaoxin.im.utils.PushUtil;
import io.realm.Realm;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("WrongConstant") public class SplashActivity extends FragmentActivity
    implements SplashView, TIMCallBack {

  @BindView(R.id.splash_img) ImageView mSplashImg;
  private SplashPresenter presenter;
  private final int REQUEST_PHONE_PERMISSIONS = 0;
  private int LOGIN_RESULT_CODE = 100;
  private static final String TAG = SplashActivity.class.getSimpleName();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    if (intent != null&& !TextUtils.isEmpty(intent.getStringExtra("app_name"))) {
      String tName = intent.getStringExtra("app_name");
      String url = intent.getStringExtra("url");
      if (tName != null) {
        Intent redirectIntent = new Intent();
        redirectIntent.setClass(SplashActivity.this, H5WebViewActivity.class);
        redirectIntent.putExtra("app_name", tName);
        redirectIntent.putExtra("url", url);
        startActivity(redirectIntent);
      }
    }else{
      clearNotification();
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
      setContentView(R.layout.activity_splash);
      ButterKnife.bind(this);
      initPermision();
    }

  }

  /**
   * 初始化权限
   */
  private void initPermision() {
    final List<String> permissionsList = new ArrayList<>();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if ((checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
          != PackageManager.PERMISSION_GRANTED)) {
        permissionsList.add(Manifest.permission.READ_PHONE_STATE);
      }
      if ((checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED)) {
        permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
      }
      if (permissionsList.size() == 0) {
        init();
      } else {
        requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
            REQUEST_PHONE_PERMISSIONS);
      }
    } else {
      init();
    }
  }

  private void init() {
    //初始化数据库框架
    Realm.init(this);
    //初始化IMSDK
    SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
    int loglvl = pref.getInt("loglvl", TIMLogLevel.DEBUG.ordinal());
    InitBusiness.start(getApplicationContext(), loglvl);
    //初始化TLS
    TlsBusiness.init(getApplicationContext());
    String id = TLSService.getInstance().getLastUserIdentifier();
    UserInfo.getInstance().setId(id);
    UserInfo.getInstance().setUserSig(TLSService.getInstance().getUserSig(id));
    presenter = new SplashPresenter(this);
    presenter.start();
  }

  /**
   * 清楚所有通知栏通知
   */
  private void clearNotification() {
    NotificationManager notificationManager =
        (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
    notificationManager.cancelAll();
    MiPushClient.clearNotification(getApplicationContext());
  }

  @Override public void onError(int i, String s) {
    switch (i) {
      case 6208:
        //离线状态下被其他终端踢下线
        NotifyDialog dialog = new NotifyDialog();
        dialog.show(getString(R.string.kick_logout), getSupportFragmentManager(),
            new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int which) {
                navToHome();
              }
            });
        break;
      case 6200:
        Toast.makeText(this, getString(R.string.login_error_timeout), Toast.LENGTH_SHORT).show();
        navToLogin();
        break;
      default:
        Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_SHORT).show();
        navToLogin();
        break;
    }
  }

  @Override public void onSuccess() {
    //初始化程序后台后消息推送
    PushUtil.getInstance();
    //初始化消息监听
    MessageEvent.getInstance();
    String deviceMan = Build.MANUFACTURER;
    //注册小米和华为推送
    if (deviceMan.equals("Xiaomi") && shouldMiInit()) {
      MiPushClient.registerPush(getApplicationContext(), "2882303761517480335", "5411748055335");
    } else if (deviceMan.equals("HUAWEI")) {
      PushManager.requestToken(this);
    }
    Intent intent = new Intent(this, HomeActivity.class);
    startActivity(intent);
    finish();
  }

  @Override public void navToHome() {
    //登录之前要初始化群和好友关系链缓存
    TIMUserConfig userConfig = new TIMUserConfig();
    userConfig.setUserStatusListener(new TIMUserStatusListener() {
      @Override public void onForceOffline() {
        Log.d(TAG, "receive force offline message");
        Intent intent = new Intent(SplashActivity.this, DialogActivity.class);
        startActivity(intent);
      }

      @Override public void onUserSigExpired() {
        //票据过期，需要重新登录
        new NotifyDialog().show(getString(R.string.tls_expire), getSupportFragmentManager(),
            new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int which) {
                //                            logout();
              }
            });
      }
    }).setConnectionListener(new TIMConnListener() {
      @Override public void onConnected() {
        Log.i(TAG, "onConnected");
      }

      @Override public void onDisconnected(int code, String desc) {
        Log.i(TAG, "onDisconnected");
      }

      @Override public void onWifiNeedAuth(String name) {
        Log.i(TAG, "onWifiNeedAuth");
      }
    });

    //设置刷新监听
    RefreshEvent.getInstance().init(userConfig);
    userConfig = FriendshipEvent.getInstance().init(userConfig);
    userConfig = GroupEvent.getInstance().init(userConfig);

    TIMManager.getInstance().setUserConfig(userConfig);
    LoginBusiness.loginIm(UserInfo.getInstance().getId(), UserInfo.getInstance().getUserSig(),
        this);
  }

  @Override public void navToLogin() {
    Intent intent = new Intent(getApplicationContext(), HostLoginActivity.class);
    startActivityForResult(intent, LOGIN_RESULT_CODE);
  }

  @Override public boolean isUserLogin() {
    return UserInfo.getInstance().getId() != null && (!TLSService.getInstance()
        .needLogin(UserInfo.getInstance().getId()));
  }

  /**
   * 判断小米推送是否已经初始化
   */
  private boolean shouldMiInit() {
    ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
    List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
    String mainProcessName = getPackageName();
    int myPid = Process.myPid();
    for (ActivityManager.RunningAppProcessInfo info : processInfos) {
      if (info.pid == myPid && mainProcessName.equals(info.processName)) {
        return true;
      }
    }
    return false;
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.d(TAG, "onActivityResult code:" + requestCode);
    if (LOGIN_RESULT_CODE == requestCode) {
      Log.d(TAG, "login error no " + TLSService.getInstance().getLastErrno());
      if (0 == TLSService.getInstance().getLastErrno()) {
        String id = TLSService.getInstance().getLastUserIdentifier();
        UserInfo.getInstance().setId(id);
        UserInfo.getInstance().setUserSig(TLSService.getInstance().getUserSig(id));
        navToHome();
      } else if (resultCode == RESULT_CANCELED) {
        finish();
      }
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mSplashImg.setBackgroundResource(0);
    System.gc();
    System.runFinalization();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case REQUEST_PHONE_PERMISSIONS: {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          init();

        } else {
            finish();
          // permission denied, boo! Disable the
          // functionality that depends on this permission.
        }
        return;
      }
    }
  }
}
