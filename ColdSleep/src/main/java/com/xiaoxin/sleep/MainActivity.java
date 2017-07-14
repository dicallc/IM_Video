package com.xiaoxin.sleep;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.socks.library.KLog;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.library.utils.RecycleViewDivider;
import com.xiaoxin.library.utils.Utils;
import com.xiaoxin.sleep.adapter.AppListAdapter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  @BindView(R.id.app_list) RecyclerView mAppList;

  List<AppInfo> list = new ArrayList<>();
  List<AppInfo> EnList = new ArrayList<>();
  List<AppInfo> Dislist = new ArrayList<>();
  private AppListAdapter mAdapter;
  private MaterialDialog mDialog;
  private List<AppInfo> mAllUserAppInfos;
  private FloatingActionButton mFab;
  private String selectedItem = LibraryCons.SELECTENABLE;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    initView();
    initData();
  }

  Runnable loadDisAppThread = new Runnable() {
    @Override public void run() {
      mAllUserAppInfos = Utils.getAllUserAppInfos(MainActivity.this);
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
            list.add(mAppInfo);
            EnList.add(mAppInfo);
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
      KLog.e("Dislist" + Dislist);

      runOnUiThread(new Runnable() {
        @Override public void run() {
          mAdapter.notifyDataSetChanged();
          goneloadDialog();
        }
      });
    }
  };
  Runnable loadEnAppThread = new Runnable() {
    @Override public void run() {
      List<AppInfo> mAllUserAppInfos = Utils.getAllUserAppInfos(MainActivity.this);
      ShellUtils.CommandResult mCommandResult =
          ShellUtils.execCommand(LibraryCons.allEnablePackageV3, true, true);
      String mSuccessMsg = mCommandResult.successMsg;
      String[] mSplit = mSuccessMsg.split("package:");
      for (int i = 1; i < mSplit.length; i++) {
        for (AppInfo mAppInfo : mAllUserAppInfos) {
          if (mSplit[i].equals(mAppInfo.packageName)) {
            mAppInfo.isSelect = false;
            list.add(mAppInfo);
          }
        }
      }
      runOnUiThread(new Runnable() {
        @Override public void run() {
          mAdapter.notifyDataSetChanged();
        }
      });
    }
  };

  private void initData() {
    initListData();
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
        }
        return false;
      }
    });
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
}
