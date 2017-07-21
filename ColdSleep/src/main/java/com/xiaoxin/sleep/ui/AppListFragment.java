package com.xiaoxin.sleep.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.library.utils.RecycleViewDivider;
import com.xiaoxin.sleep.AppDao;
import com.xiaoxin.sleep.R;
import com.xiaoxin.sleep.adapter.AppListAdapter;
import com.xiaoxin.sleep.model.Event;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AppListFragment extends Fragment {

  @BindView(R.id.app_list) RecyclerView mAppList;
  Unbinder unbinder;

  List<AppInfo> list = new ArrayList<>();
  private AppListAdapter mAdapter;

  public AppListFragment() {
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
  }

  private void initRecylerView() {
    mAppList.setLayoutManager(new LinearLayoutManager(getActivity()));
    mAppList.addItemDecoration(new RecycleViewDivider(getActivity(), LinearLayoutManager.VERTICAL,
        R.drawable.divider_mileage));
    mAdapter = new AppListAdapter(list);
    mAppList.setAdapter(mAdapter);
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void onAppDaoMessage(Event msg) {
    switch (msg.getCurrentDay()) {
      case Event.MONDAY: {
        list.clear();
        List<AppInfo> mAllUserAppList = AppDao.getInstance().getAllUserAppList();
        list.addAll(mAllUserAppList);
        break;
      }
    }
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
