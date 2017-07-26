package com.xiaoxin.sleep.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.library.utils.Utils;
import com.xiaoxin.sleep.AppDao;
import com.xiaoxin.sleep.R;
import com.xiaoxin.sleep.ShellUtils;
import com.xiaoxin.sleep.adapter.AppListAdapter;
import com.xiaoxin.sleep.common.BaseActivity;
import com.xiaoxin.sleep.model.Event;
import com.xiaoxin.sleep.view.DialogWithCircularReveal;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BaseActivity implements BaseQuickAdapter.OnItemClickListener {

  @BindView(R.id.app_list) RecyclerView mAppList;

  List<AppInfo> list = new ArrayList<>();
  List<AppInfo> other_list = new ArrayList<>();
  @BindView(R.id.fab) FloatingActionButton mFab;
  @BindView(R.id.toolbar) Toolbar mToolbar;
  @BindView(R.id.other_app_list) RecyclerView mOtherAppList;
  private AppListAdapter mAdapter;
  private List<AppInfo> mAllUserAppInfos = new ArrayList<>();
  private ViewCompleteImpl mViewComplete;
  private AppListAdapter mOtherAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    EventBus.getDefault().register(this);
    ButterKnife.bind(this);
    initView();
    initData();
    isIgnoreBatteryOption(this);
    mViewComplete = new ViewCompleteImpl();
    getContentView().getViewTreeObserver().addOnGlobalLayoutListener(mViewComplete);
  }

  private void initData() {
    List<AppInfo> sList = AppDao.getInstance().getUserSaveDisAppFromDB();
    AppDao.getInstance().SyncDisData(mActivity);
    list.clear();
    list.addAll(sList);
    mAdapter.setOnItemClickListener(this);
    mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {

      private DialogWithCircularReveal dialog;

      @Override public boolean onItemLongClick(final BaseQuickAdapter mBaseQuickAdapter, View mView,
          final int mI) {
        final List<AppInfo> mData = mBaseQuickAdapter.getData();
        final AppInfo mAppInfo = mData.get(mI);
        View mInflate = View.inflate(MainActivity.this, R.layout.dialog_dis_content_normal, null);
        TextView uninstall_app = (TextView) mInflate.findViewById(R.id.fuc_delete);
        TextView fuc_remoove = (TextView) mInflate.findViewById(R.id.fuc_remoove);
        TextView fuc_wake = (TextView) mInflate.findViewById(R.id.fuc_wake);
        TextView app_title = (TextView) mInflate.findViewById(R.id.app_title);
        ImageView app_icon = (ImageView) mInflate.findViewById(R.id.app_icon);
        app_title.setText(mAppInfo.appName);
        Glide.with(MainActivity.this).load(mAppInfo.file_path).into(app_icon);
        //解冻
        fuc_wake.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            WarnApp(mAppInfo);
          }
        });
        //移出列表
        fuc_remoove.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            dialog.dismiss();
            showloadDialog("操作中");
            WarnApp(mAppInfo);
            mBaseQuickAdapter.getData().remove(mAppInfo);
            mBaseQuickAdapter.notifyDataSetChanged();
            //缓存
            AppDao.getInstance().saveUserSaveDisAppToDB(mData);
          }
        });
        uninstall_app.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            dialog.dismiss();
            ShellUtils.execCommand(LibraryCons.uninstall_app + mAppInfo.packageName, true, true);
            mBaseQuickAdapter.getData().remove(mAppInfo);
            mAdapter.notifyDataSetChanged();
            //缓存
            AppDao.getInstance().saveUserSaveDisAppToDB(mData);
          }
        });
        dialog = new DialogWithCircularReveal(MainActivity.this, mInflate);
        dialog.setRevealview(R.id.myView);
        dialog.showDialog();
        return true;
      }
    });
  }

  private void WarnApp(AppInfo mAppInfo) {
    ShellUtils.execCommand(LibraryCons.make_app_to_enble + mAppInfo.packageName, true, true);
    mAppInfo.isWarn = true;
  }

  private void openApp(AppInfo mAppInfo) {
    Utils.openApp(MainActivity.this, mAppInfo.packageName);
    mAdapter.notifyDataSetChanged();
  }

  @OnClick(R.id.fab) public void onFabClicked() {
    sleepApp();
  }

  private void sleepApp() {
    showloadDialog("正在冷冻中");
    //找出已经解冻的app进行冻结
    List<AppInfo> sList=new ArrayList<>();
    List<AppInfo> mData = mAdapter.getData();
    findWarnApp(mData);
    mAdapter.notifyDataSetChanged();
    List<AppInfo> mOtherAdapterData = mOtherAdapter.getData();
    findWarnApp(mOtherAdapterData);
    mOtherAdapter.notifyDataSetChanged();
    goneloadDialog();
    //缓存
    sList.addAll(mData);
    sList.addAll(mOtherAdapterData);
    AppDao.getInstance().saveUserSaveDisAppToDB(mData);
  }

  private void findWarnApp(List<AppInfo> mData) {
    for (AppInfo mAppInfo : mData) {
      if (mAppInfo.isWarn) {
        ShellUtils.execCommand(LibraryCons.make_app_to_disenble + mAppInfo.packageName, true, true);
        mAppInfo.isWarn = false;
      }
    }
  }

  private void initView() {
    initRecylerView();
    setSupportActionBar(mToolbar);
  }

  private void initRecylerView() {
    initAppList();
    initOther();
  }

  private void initOther() {
    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 4);
    mGridLayoutManager.setSmoothScrollbarEnabled(true);
    mGridLayoutManager.setAutoMeasureEnabled(true);
    mOtherAppList.setLayoutManager(mGridLayoutManager);
    mOtherAppList.setHasFixedSize(true);
    mOtherAppList.setNestedScrollingEnabled(false);
    mOtherAdapter = new AppListAdapter(other_list);
    mOtherAppList.setAdapter(mOtherAdapter);
  }

  private void initAppList() {
    GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 4);
    mGridLayoutManager.setSmoothScrollbarEnabled(true);
    mGridLayoutManager.setAutoMeasureEnabled(true);
    mAppList.setLayoutManager(mGridLayoutManager);
    mAppList.setHasFixedSize(true);
    mAppList.setNestedScrollingEnabled(false);
    mAdapter = new AppListAdapter(list);
    mAppList.setAdapter(mAdapter);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int position) {
    //打开app先解冻
    List<AppInfo> mData = mBaseQuickAdapter.getData();
    AppInfo mAppInfo = mData.get(position);
    mAppInfo.open_num++;
    WarnApp(mAppInfo);
    openApp(mAppInfo);
    //缓存
    AppDao.getInstance().saveUserSaveDisAppToDB(mData);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    Intent mIntent = new Intent(mActivity, SelectAppActivity.class);
    mIntent.putExtra(LibraryCons.ACTION, LibraryCons.ACTION_OPEN);
    startActivity(mIntent);
    return super.onOptionsItemSelected(item);
  }

  class ViewCompleteImpl implements ViewTreeObserver.OnGlobalLayoutListener {

    @Override public void onGlobalLayout() {
      revealShow(getContentView());
      getContentView().getViewTreeObserver().removeOnGlobalLayoutListener(mViewComplete);
    }
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void onAppDaoMessage(Event msg) {
    switch (msg.getCurrentDay()) {
      case Event.getDisList:
        List<AppInfo> lists = msg.list;
        list.clear();
        other_list.clear();
        //排序取出8个使用频率比较高的
        List<AppInfo> mAppInfos = AppDao.getInstance().sortAppList(lists);
        list.addAll(mAppInfos);
        other_list.addAll(lists.subList(8,lists.size()));
        mOtherAdapter.notifyDataSetChanged();
        mAdapter.notifyDataSetChanged();
        break;
    }
  }

  @Override protected void onDestroy() {
    EventBus.getDefault().unregister(this);
    super.onDestroy();
  }
}
