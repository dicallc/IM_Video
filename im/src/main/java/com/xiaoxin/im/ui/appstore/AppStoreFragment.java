package com.xiaoxin.im.ui.appstore;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arialyy.aria.core.Aria;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.socks.library.KLog;
import com.tencent.qcloud.ui.TemplateTitle;
import com.xiaoxin.im.R;
import com.xiaoxin.im.common.onConnectionFinishLinstener;
import com.xiaoxin.im.model.AppCacheModel;
import com.xiaoxin.im.model.AppStoreModel;
import com.xiaoxin.im.ui.appstore.adapter.AppStoreAdapter;
import com.xiaoxin.im.ui.appstore.dao.AppStoreDao;
import com.xiaoxin.im.ui.appstore.impl.MySchedulerListener;
import com.xiaoxin.im.ui.appstore.ui.AppWebViewActivity;
import com.xiaoxin.im.ui.customview.DividerGridItemDecoration;
import com.xiaoxin.im.utils.AppUpdateUtils;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
  private String mAppTitle;
  private AppStoreModel.ContentBean mDownAppModel;
  private Realm mRealm;

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
    EventBus.getDefault().register(this);
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

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    initView();
    initData();
  }

  private void initData() {
    AppStoreDao.getAppList(new onConnectionFinishLinstener() {
      @Override public void onSuccess(int code, Object result) {
        mlist.clear();
        AppStoreModel models = (AppStoreModel) result;
        List<AppStoreModel.ContentBean> content = models.content;
        mlist.addAll(content);
        adapter.notifyDataSetChanged();
      }

      @Override public void onFail(int code, String result) {
        KLog.e(result);
      }
    });
  }

  private void initView() {
    mAppList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
    mAppList.addItemDecoration(new DividerGridItemDecoration(getActivity()));
    adapter = new AppStoreAdapter(mlist);
    adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
      @Override public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
        List<AppStoreModel.ContentBean> mData = mBaseQuickAdapter.getData();
        mDownAppModel = mData.get(mI);
        mAppTitle = mDownAppModel.title;
        //检查文件是否存在
        if (AppUpdateUtils.checkFile(getActivity(),mAppTitle)){
          //比较服务器版本和本地版本
          if (checkAppVersion()){
            //显示更新框
            showUpdateDialog();
          }else{
            //直接打开
            openWebView();
          }
        }else{
          startDown(mDownAppModel.down_path, mAppTitle);
        }

      }
    });
    mAppList.setAdapter(adapter);
  }

  private void showUpdateDialog() {
    MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
        .title(mDownAppModel.title+"有新版本可升级")
        .content(mDownAppModel.version_des)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(@NonNull MaterialDialog mMaterialDialog,
              @NonNull DialogAction mDialogAction) {
            startDown(mDownAppModel.down_path,mDownAppModel.title);
          }
        })
        .onNegative(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(@NonNull MaterialDialog mMaterialDialog,
              @NonNull DialogAction mDialogAction) {
            openWebView();
          }
        })
        .negativeText("不升级")
        .positiveText(R.string.agree);

    MaterialDialog dialog = builder.build();
    dialog.show();
  }

  private boolean checkAppVersion() {
    getRelam();
    RealmQuery<AppCacheModel> query = mRealm.where(AppCacheModel.class);
    query.equalTo("AppName", mDownAppModel.title);
    RealmResults<AppCacheModel> result1 = query.findAll();
    if (result1.size()!=0){
      AppCacheModel mCacheModel = result1.get(0);
      //比较版本
      if (AppUpdateUtils.compareVersion(mCacheModel.AppVersion,mDownAppModel.version)==-1){
        return true;
      }else{
        return false;
      }
    }

    return false;
  }

  private void getRelam() {
    if (null==mRealm)
      mRealm = Realm.getDefaultInstance();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    EventBus.getDefault().unregister(this);
    unbinder.unbind();
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onEventMainThread(AppStoreModel mModel) {
    openWebView();
    SaveAppSqlCache();
  }

  /**
   * 保存下载app缓存
   */
  private void SaveAppSqlCache() {
    getRelam();
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        // Add a person
        AppCacheModel mCacheModel = realm.createObject(AppCacheModel.class);
        mCacheModel.AppName = mDownAppModel.title;
        mCacheModel.AppVersion =mDownAppModel.version;
      }
    });
  }

  private void openWebView() {
    Intent mIntent = new Intent(getActivity(), AppWebViewActivity.class);
    mIntent.putExtra("name", mAppTitle);
    startActivity(mIntent);
  }

  private void startDown(String url, String name) {
    Aria.download(getActivity()).addSchedulerListener(new MySchedulerListener(name, getActivity()));
    Aria.download(this)
        //.load("http://shashayuapp.oss-cn-shenzhen.aliyuncs.com/ppmMember.zip")
        .load(url)
        //.setDownloadPath(Environment.getExternalStorageDirectory().getPath() + "/oa.zip")  //文件保存路径
        //.setDownloadPath(getActivity().getFilesDir() + "/oa.zip")  //文件保存路径
        .setDownloadPath(getActivity().getFilesDir() + "/" + name + ".zip")  //文件保存路径
        .start();   //启动下载
  }
}
