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
      KLog.e("Dislist"+Dislist);

      runOnUiThread(new Runnable() {
        @Override public void run() {
          mAdapter.notifyDataSetChanged();
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
    new Thread(loadDisAppThread).start();
    mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int position) {
        AppInfo mAppInfo = list.get(position);
        mAppInfo.isSelect = !mAppInfo.isSelect;
        mAdapter.notifyDataSetChanged();
      }
    });
  }

  private void initFloatBar() {
    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        for (AppInfo mAppInfo : list) {
          if (mAppInfo.isSelect)
          ShellUtils.execCommand(LibraryCons.make_app_to_enble+mAppInfo.packageName, true, true);
        }
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
      //切换已冷冻
      mAdapter.getData().clear();
      mAdapter.getData().addAll(Dislist);
    }else if(id == R.id.action_cold){
      //切换到未冷冻
      mAdapter.getData().clear();
      mAdapter.getData().addAll(EnList);
    }else {
      //切换到全部app
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
    if (null==mDialog)
      mDialog.dismiss();

  }
}
