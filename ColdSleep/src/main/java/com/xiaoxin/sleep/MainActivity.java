package com.xiaoxin.sleep;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.library.utils.RecycleViewDivider;
import com.xiaoxin.library.utils.SpUtils;
import com.xiaoxin.library.utils.Utils;
import com.xiaoxin.sleep.adapter.AppListAdapter;
import com.xiaoxin.sleep.model.AppCache;
import com.xiaoxin.sleep.service.StopAppService;
import com.xiaoxin.sleep.utils.ToastUtils;
import java.util.ArrayList;
import java.util.List;
import me.shaohui.bottomdialog.BottomDialog;

public class MainActivity extends AppCompatActivity {

  private static int REQUEST_IGNORE_BATTERY_CODE;
  @BindView(R.id.app_list) RecyclerView mAppList;

  List<AppInfo> list = new ArrayList<>();
  List<AppInfo> EnList = new ArrayList<>();
  List<AppInfo> Dislist = new ArrayList<>();
  private AppListAdapter mAdapter;
  private MaterialDialog mDialog;
  private List<AppInfo> mAllUserAppInfos = new ArrayList<>();
  private FloatingActionButton mFab;
  private String selectedItem = LibraryCons.SELECTENABLE;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    initView();
    initData();
    //wakeService();
    isIgnoreBatteryOption(this);
  }

  private void wakeService() {
    Intent grayIntent = new Intent(getApplicationContext(), StopAppService.class);
    startService(grayIntent);
  }

  Runnable loadLocalDBRunnle = new Runnable() {
    @Override public void run() {
      getLocalCache();
    }
  };
  Runnable loadDisAppThread = new Runnable() {
    @Override public void run() {
      clearCache();
      long start = System.currentTimeMillis();
      mAllUserAppInfos = Utils.getAllUserAppInfos(MainActivity.this);
      long end = System.currentTimeMillis();
      //获取未被禁用的app列表
      ShellUtils.CommandResult allEnAppsRes =
          ShellUtils.execCommand(LibraryCons.allEnablePackageV3, true, true);
      String mAllEnMsg = allEnAppsRes.successMsg;
      String[] mSplit = mAllEnMsg.split("package:");
      ShellUtils.CommandResult allDisAppsRes =
          ShellUtils.execCommand(LibraryCons.allDisabledPackage, true, true);
      String[] DisApps = allDisAppsRes.successMsg.split("package:");
      for (int i = 1; i < mSplit.length; i++) {
        for (AppInfo mAppInfo : mAllUserAppInfos) {
          if (mSplit[i].equals(mAppInfo.packageName)) {
            mAppInfo.isSelect = false;
            EnList.add(mAppInfo);
            //看缓存列表是否存在不存在就添加
            checkCacheExit(mAppInfo);
          }
        }
      }
      for (int i = 1; i < DisApps.length; i++) {
        for (AppInfo mAppInfo : mAllUserAppInfos) {
          if (DisApps[i].equals(mAppInfo.packageName)) {
            mAppInfo.isSelect = true;
            Dislist.add(mAppInfo);
          }
        }
      }
      saveLocalCache();
      //KLog.e("Dislist" + Dislist);

      runOnUiThread(new Runnable() {
        @Override public void run() {
          if (0 == mAdapter.getData().size()) {
            list.addAll(EnList);
          }
          mAdapter.notifyDataSetChanged();
          goneloadDialog();
        }
      });
    }

    private void checkCacheExit(AppInfo mAppInfo) {
      int index = 0;
      for (int j = 0; j < list.size(); j++) {
        AppInfo model = list.get(j);
        if (model.appName.equals(mAppInfo.appName)) index++;
        if (j == list.size() - 1 && index == 0) {
          list.add(mAppInfo);
        }
      }
    }
  };

  private void clearCache() {
    //    list.clear();
    EnList.clear();
    Dislist.clear();
    mAllUserAppInfos.clear();
  }

  private void SetMemoryCache(AppCache mAppCache) {
    EnList.addAll(mAppCache.en);
    Dislist.addAll(mAppCache.dis);
    mAllUserAppInfos.addAll(mAppCache.all);
  }

  private void saveLocalCache() {
    AppCache mAppCache = new AppCache(EnList, Dislist, mAllUserAppInfos);
    String mJson = new Gson().toJson(mAppCache);
    SpUtils.setParam(MainActivity.this, LibraryCons.LOCAL_DB_NAME, mJson);
  }

  private void initData() {
    showloadDialog("获取列表中");
    new Thread(loadLocalDBRunnle).start();
    mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int position) {
        AppInfo mAppInfo = list.get(position);
        mAppInfo.isSelect = !mAppInfo.isSelect;
        mAdapter.notifyDataSetChanged();
      }
    });
    mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(final BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
        final AppInfo mAppInfo = (AppInfo) mBaseQuickAdapter.getData().get(mI);
        if (LibraryCons.SELECTDisabled.equals(selectedItem)) {
          new MaterialDialog.Builder(MainActivity.this).title("操作")
              .items(R.array.sleep_custion_function)
              .itemsCallback(new MaterialDialog.ListCallback() {
                @Override public void onSelection(MaterialDialog dialog, View view, int which,
                    CharSequence text) {
                  //消失操作框
                  dialog.dismiss();
                  showloadDialog("操作中");
                  ShellUtils.execCommand(LibraryCons.make_app_to_enble + mAppInfo.packageName, true,
                      true);
                  mBaseQuickAdapter.getData().remove(mAppInfo);
                  initListData();
                }
              })
              .show();
          return true;
        } else if (LibraryCons.SELECTENABLE.equals(selectedItem)) {
          BottomDialog.create(getSupportFragmentManager())
              .setLayoutRes(R.layout.dialog_content_normal)
              .setViewListener(new BottomDialog.ViewListener() {
                @Override public void bindView(View mView) {
                  TextView mViewById = (TextView) mView.findViewById(R.id.fuc_delete);
                  TextView app_title = (TextView) mView.findViewById(R.id.app_title);
                  ImageView app_icon = (ImageView) mView.findViewById(R.id.app_icon);
                  app_title.setText(mAppInfo.appName);
                  Glide.with(MainActivity.this).load(mAppInfo.file_path).into(app_icon);
                  mViewById.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                      ToastUtils.showShortToast("点击了");
                    }
                  });
                }
              })
              .show();
        }
        return false;
      }
    });
  }

  private void getLocalCache() {
    String json = (String) SpUtils.getParam(MainActivity.this, LibraryCons.LOCAL_DB_NAME, "");
    if (!TextUtils.isEmpty(json)) {
      AppCache mAppCache = new Gson().fromJson(json, AppCache.class);
      clearCache();
      SetMemoryCache(mAppCache);
      list.addAll(mAppCache.en);
      runOnUiThread(new Runnable() {
        @Override public void run() {
          mAdapter.notifyDataSetChanged();
          goneloadDialog();
          initListData();
        }
      });
    } else {
      initListData();
    }
  }

  private void initListData() {
    new Thread(loadDisAppThread).start();
  }

  private void initFloatBar() {
    mFab = (FloatingActionButton) findViewById(R.id.fab);
    mFab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        showloadDialog("正在冷冻中");
        List<AppInfo> part = new ArrayList<>();
        List<AppInfo> mData = mAdapter.getData();
        for (AppInfo mAppInfo : mData) {
          if (mAppInfo.isSelect) {
            ShellUtils.execCommand(LibraryCons.make_app_to_disenble + mAppInfo.packageName, true,
                true);
            part.add(mAppInfo);
          }
        }
        mData.removeAll(part);
        initListData();
      }
    });
  }

  private void initView() {
    //showloadDialog("加载列表中");
    initRecylerView();
    initToobar();
    initFloatBar();
  }

  private void initRecylerView() {
    mAppList.setLayoutManager(new LinearLayoutManager(this));
    mAppList.addItemDecoration(
        new RecycleViewDivider(this, LinearLayoutManager.VERTICAL, R.drawable.divider_mileage));
    mAdapter = new AppListAdapter(list);
    mAppList.setAdapter(mAdapter);
  }

  private void initToobar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_colded) {
      //切换已冷冻 记录标识符
      selectedItem = LibraryCons.SELECTDisabled;
      mFab.setVisibility(View.GONE);
      mAdapter.getData().clear();
      mAdapter.getData().addAll(Dislist);
    } else if (id == R.id.action_cold) {
      //切换到未冷冻
      selectedItem = LibraryCons.SELECTENABLE;
      mFab.setVisibility(View.VISIBLE);
      mAdapter.getData().clear();
      mAdapter.getData().addAll(EnList);
    } else {
      //切换到全部app
      selectedItem = LibraryCons.SELECTEAll;
      mFab.setVisibility(View.GONE);
      mAdapter.getData().clear();
      mAdapter.getData().addAll(mAllUserAppInfos);
    }
    mAdapter.notifyDataSetChanged();
    return true;
  }

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

  @TargetApi(Build.VERSION_CODES.M)
  public static boolean isIgnoringBatteryOptimizations(Activity activity) {
    String packageName = activity.getPackageName();
    PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
    if (pm.isIgnoringBatteryOptimizations(packageName)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 针对N以上的Doze模式
   */
  public static void isIgnoreBatteryOption(Activity activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      try {
        Intent intent = new Intent();
        String packageName = activity.getPackageName();
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
          //               intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
          intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
          intent.setData(Uri.parse("package:" + packageName));
          activity.startActivityForResult(intent, REQUEST_IGNORE_BATTERY_CODE);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      //if (requestCode == BatteryUtils.REQUEST_IGNORE_BATTERY_CODE){
      //    //TODO something
      //}
    } else if (resultCode == RESULT_CANCELED) {
      //if (requestCode == BatteryUtils.REQUEST_IGNORE_BATTERY_CODE){
      //    ToastUtils.show(getActivity(), "请开启忽略电池优化~");
      //}
    }
  }
}
