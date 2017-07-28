package com.xiaoxin.sleep.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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
import com.xiaoxin.sleep.adapter.OtherAppListAdapter;
import com.xiaoxin.sleep.common.BaseActivity;
import com.xiaoxin.sleep.model.Event;
import com.xiaoxin.sleep.utils.ToastUtils;
import com.xiaoxin.sleep.view.DialogWithCircularReveal;
import com.xiaoxin.sleep.view.SleepHeaderView;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BaseActivity
    implements BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemLongClickListener {

  List<AppInfo> other_list = new ArrayList<>();
  @BindView(R.id.fab) FloatingActionButton mFab;
  @BindView(R.id.toolbar) Toolbar mToolbar;
  @BindView(R.id.other_app_list) RecyclerView mOtherAppList;
  private ViewCompleteImpl mViewComplete;
  private OtherAppListAdapter mOtherAdapter;
  private SleepHeaderView mSleepHeaderView;

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
    List<AppInfo> headList = AppDao.getInstance().sortAppList(sList);
    initList(sList, headList);
    AppDao.getInstance().SyncDisData(mActivity);
    mSleepHeaderView.getAdapter().setOnItemClickListener(this);
    mOtherAdapter.setOnItemClickListener(this);
    mSleepHeaderView.getAdapter().setOnItemLongClickListener(this);
    mOtherAdapter.setOnItemLongClickListener(this);
  }

  private void initList(List<AppInfo> mSList, List<AppInfo> mHeadList) {
    mSleepHeaderView.setList(mHeadList);
    mOtherAdapter.setList(mSList.subList(8, mSList.size()));
  }

  private void WarnApp(AppInfo mAppInfo) {
    ShellUtils.execCommand(LibraryCons.make_app_to_enble + mAppInfo.packageName, true, true);
    mAppInfo.isWarn = true;
  }

  private void openApp(AppInfo mAppInfo) {
    Utils.openApp(MainActivity.this, mAppInfo.packageName);
    notifyDataSetChanged();
  }

  @OnClick(R.id.fab) public void onFabClicked() {
    sleepApp();
  }

  private void sleepApp() {
    showloadDialog("正在冷冻中");
    //找出已经解冻的app进行冻结
    List<AppInfo> mData = mSleepHeaderView.getAdapter().getData();
    int mWarnApp = AppDao.getInstance().findWarnApp(mData);
    List<AppInfo> mOtherAdapterData = mOtherAdapter.getData();
    int mWarnApp1 = AppDao.getInstance().findWarnApp(mOtherAdapterData);
    notifyDataSetChanged();
    //缓存
    saveUserDis(mData, mOtherAdapterData);
    goneloadDialog();
    int num = mWarnApp + mWarnApp1;
    ToastUtils.showShortToast("睡眠" + num + "个");
  }

  private void saveUserDis(List<AppInfo> mData, List<AppInfo> mOtherAdapterData) {
    List<AppInfo> sList = new ArrayList<>();
    sList.addAll(mData);
    sList.addAll(mOtherAdapterData);
    AppDao.getInstance().saveUserSaveDisAppToDB(sList);
  }

  private void saveUserDis() {
    List<AppInfo> sList = new ArrayList<>();
    sList.addAll(mSleepHeaderView.getAdapter().getData());
    sList.addAll(mOtherAdapter.getData());
    AppDao.getInstance().saveUserSaveDisAppToDB(sList);
  }

  private void initView() {
    initRecylerView();
    setSupportActionBar(mToolbar);
  }

  private void initRecylerView() {
    initSleepList();
  }

  private void initSleepList() {
    //建立headView
    mSleepHeaderView = new SleepHeaderView(mActivity);
    mOtherAppList.setLayoutManager(new GridLayoutManager(this, 4));
    mOtherAdapter = new OtherAppListAdapter(other_list);
    mOtherAdapter.addHeaderView(mSleepHeaderView);
    mOtherAppList.setAdapter(mOtherAdapter);
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
    saveUserDis();
    notifyDataSetChanged();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    Intent mIntent = new Intent(mActivity, SettingActivity.class);
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      startActivityWithOptions(mIntent);
    } else {
      startActivity(mIntent);
    }

    return super.onOptionsItemSelected(item);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) private void startActivityWithOptions(Intent intent) {
    ActivityOptions transitionActivity = null;
    transitionActivity = ActivityOptions.makeSceneTransitionAnimation(mActivity);
    startActivity(intent, transitionActivity.toBundle());
  }

  DialogWithCircularReveal dialog = null;

  @Override
  public boolean onItemLongClick(final BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
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
        notifyDataSetChanged();
      }
    });
    //移出列表
    fuc_remoove.setOnClickListener(new View.OnClickListener() {

      @Override public void onClick(View v) {
        dialog.dismiss();
        showloadDialog("操作中");
        WarnApp(mAppInfo);
        mBaseQuickAdapter.getData().remove(mAppInfo);
        notifyDataSetChanged();
        //缓存
        saveUserDis();
      }
    });
    uninstall_app.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        dialog.dismiss();
        ShellUtils.execCommand(LibraryCons.uninstall_app + mAppInfo.packageName, true, true);
        mBaseQuickAdapter.getData().remove(mAppInfo);
        notifyDataSetChanged();
        //缓存
        saveUserDis();
      }
    });
    dialog = new DialogWithCircularReveal(MainActivity.this, mInflate);
    dialog.setRevealview(R.id.myView);
    dialog.showDialog();
    return true;
  }

  private void notifyDataSetChanged() {
    mSleepHeaderView.getAdapter().notifyDataSetChanged();
    mOtherAdapter.notifyDataSetChanged();
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
        //排序取出8个使用频率比较高的
        List<AppInfo> mAppInfos = AppDao.getInstance().sortAppList(lists);
        //赋值给头部
        initList(lists, mAppInfos);
        break;
      case Event.NOTIFYADAPTER:
        List<AppInfo> mList = msg.list;
        List<AppInfo> headList = AppDao.getInstance().sortAppList(mList);
        initList(mList, headList);
        notifyDataSetChanged();
        break;
    }
  }

  @Override protected void onDestroy() {
    EventBus.getDefault().unregister(this);
    super.onDestroy();
  }
}
