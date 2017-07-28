package com.xiaoxin.sleep.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.sleep.AppDao;
import com.xiaoxin.sleep.R;
import com.xiaoxin.sleep.ShellUtils;
import com.xiaoxin.sleep.adapter.SelectAppListAdapter;
import com.xiaoxin.sleep.common.BaseFragment;
import com.xiaoxin.sleep.model.Event;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AppListFragment extends BaseFragment
    implements BaseQuickAdapter.OnItemClickListener, BaseQuickAdapter.OnItemLongClickListener {

  @BindView(R.id.app_list) RecyclerView mAppList;
  Unbinder unbinder;

  public List<AppInfo> list = new ArrayList<>();
  private SelectAppListAdapter mAdapter;

  public AppListFragment() {
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    EventBus.getDefault().register(this);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_app_list, container, false);
    unbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initRecylerView();
    AppDao.getInstance().initListData(getActivity());
  }

  private void initRecylerView() {
    mAppList.setLayoutManager(new LinearLayoutManager(getActivity()));
    mAdapter = new SelectAppListAdapter(list);
    mAppList.setAdapter(mAdapter);
    mAdapter.setOnItemClickListener(this);
    mAdapter.setOnItemLongClickListener(this);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  @Subscribe(threadMode = ThreadMode.MAIN,priority = 100) public void onAppDaoMessage(Event msg) {
    switch (msg.getCurrentDay()) {
      case Event.MONDAY:
        list.clear();
        List<AppInfo> mAllUserAppList = AppDao.getInstance().getList();
        list.addAll(mAllUserAppList);
        mAdapter.notifyDataSetChanged();
        break;
      case Event.NOTIFYADAPTER:
        mAdapter.notifyDataSetChanged();
        break;
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @Override public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
    AppInfo mAppInfo = (AppInfo) mBaseQuickAdapter.getData().get(mI);
    mAppInfo.isSelect = !mAppInfo.isSelect;
    CheckBox mCheckBox = (CheckBox) mView.findViewById(R.id.checkBox);
    if (mAppInfo.isSelect) {
      mCheckBox.setChecked(true);
    } else {
      mCheckBox.setChecked(false);
    }
  }

  @Override public boolean onItemLongClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
    showloadDialog("操作中");
    final AppInfo mAppInfo = (AppInfo) mBaseQuickAdapter.getData().get(mI);
    mAppInfo.isEnable = true;
    ShellUtils.execCommand(LibraryCons.make_app_to_enble + mAppInfo.packageName, true, true);
    mBaseQuickAdapter.notifyItemChanged(mI);
    goneloadDialog();
    AppDao.getInstance().SyncData(getActivity());
    return true;
  }
}
