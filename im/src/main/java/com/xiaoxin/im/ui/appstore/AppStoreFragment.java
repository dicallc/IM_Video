package com.xiaoxin.im.ui.appstore;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.socks.library.KLog;
import com.tencent.qcloud.ui.TemplateTitle;
import com.xiaoxin.im.R;
import com.xiaoxin.im.common.onConnectionFinishLinstener;
import com.xiaoxin.im.model.AppStoreModel;
import com.xiaoxin.im.ui.appstore.adapter.AppStoreAdapter;
import com.xiaoxin.im.ui.appstore.dao.AppStoreDao;
import com.xiaoxin.im.ui.customview.DividerGridItemDecoration;
import com.xiaoxin.im.ui.video.VideoListActivity;

import java.util.ArrayList;
import java.util.List;

public class AppStoreFragment extends Fragment {
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  @BindView(R.id.appstore_title) TemplateTitle mAppstoreTitle;
  @BindView(R.id.app_list) RecyclerView mAppList;
  Unbinder unbinder;

  List<AppStoreModel.ContentBean> mlist = new ArrayList<>();

  private String mParam1;
  private String mParam2;
  private AppStoreAdapter adapter;

  public AppStoreFragment() {
  }

  public static AppStoreFragment newInstance(String param1, String param2) {
    AppStoreFragment fragment = new AppStoreFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_appstore, container, false);
    unbinder = ButterKnife.bind(this, view);
    return view;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView();
    initData();
  }

  private void initData() {
    AppStoreDao.getAppList(new onConnectionFinishLinstener() {
      @Override
      public void onSuccess(int code, Object result) {
        mlist.clear();
        AppStoreModel models = (AppStoreModel) result;
        List<AppStoreModel.ContentBean> content = models.content;
        mlist.addAll(content);
        adapter.notifyDataSetChanged();
      }

      @Override
      public void onFail(int code, String result) {
        KLog.e(result);
      }
    });
  }

  private void initView() {
    mAppList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
    mAppList.addItemDecoration(new DividerGridItemDecoration(getActivity()));
    adapter = new AppStoreAdapter(mlist);
    mAppList.setAdapter(adapter);
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }
}
