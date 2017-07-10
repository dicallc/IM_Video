package com.xiaoxin.im.ui.appstore;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arialyy.aria.core.Aria;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
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
import com.xiaoxin.im.ui.appstore.ui.H5WebViewActivity;
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
  private Uri fileUri;
  private EditText app_name;
  private TextInputLayout name_ly;
  private EditText app_path;
  private TextInputLayout app_path_ly;
  private CheckBox checkBox;
  private ImageView mAdd_img;

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
    initView();
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
    mAppstoreTitle.setMoreImgAction(new View.OnClickListener() {
      @Override public void onClick(View v) {
        ShowAddAppDialog();
      }
    });
    mAppList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
    mAppList.addItemDecoration(new DividerGridItemDecoration(getActivity()));
    adapter = new AppStoreAdapter(mlist);
    adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
      @Override public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
        List<AppStoreModel.ContentBean> mData = mBaseQuickAdapter.getData();
        mDownAppModel = mData.get(mI);
        //cheAppType(mDownAppModel);
        if ("h5".equals(mDownAppModel.app_type)) {
          openH5WebView(mDownAppModel);
          return;
        }
        mAppTitle = mDownAppModel.title;
        //检查文件是否存在
        if (AppUpdateUtils.checkFile(getActivity(), mAppTitle)) {
          //比较服务器版本和本地版本
          if (checkAppVersion()) {
            //显示更新框
            showUpdateDialog();
          } else {
            //直接打开
            openWebView();
          }
        } else {
          startDown(mDownAppModel.down_path, mAppTitle);
        }
      }
    });
    mAppList.setAdapter(adapter);
  }

  private void ShowAddAppDialog() {
    View view = View.inflate(getActivity(), R.layout.appstore_add_app, null);
    app_name = (EditText) view.findViewById(R.id.app_name);
    mAdd_img = (ImageView) view.findViewById(R.id.add);
    mAdd_img.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showPhoto();
      }
    });
    name_ly = (TextInputLayout) view.findViewById(R.id.name_ly);
    //name_ly.setOnClickListener(this);
    app_path = (EditText) view.findViewById(R.id.app_path);
    //app_path.setOnClickListener(this);
    app_path_ly = (TextInputLayout) view.findViewById(R.id.app_path_ly);
    //app_path_ly.setOnClickListener(this);
    checkBox = (CheckBox) view.findViewById(R.id.checkBox);
    new MaterialDialog.Builder(getActivity()).title("微应用添加")
        .customView(view, false)
        .positiveText("添加")
        .negativeText("取消")
        .show();
  }

  private void openH5WebView(AppStoreModel.ContentBean mDownAppModel) {
    Intent mIntent = new Intent(getActivity(), H5WebViewActivity.class);
    mIntent.putExtra("url", mDownAppModel.down_path);
    startActivity(mIntent);
  }

  private void cheAppType(AppStoreModel.ContentBean mDownAppModel) {
    //if ("ionic".equals(mDownAppModel.app_type))
    //  re
  }

  private void showUpdateDialog() {
    MaterialDialog.Builder builder =
        new MaterialDialog.Builder(getActivity()).title(mDownAppModel.title + "有新版本可升级")
            .content(mDownAppModel.version_des)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
              @Override public void onClick(@NonNull MaterialDialog mMaterialDialog,
                  @NonNull DialogAction mDialogAction) {
                startDown(mDownAppModel.down_path, mDownAppModel.title);
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
    if (result1.size() != 0) {
      AppCacheModel mCacheModel = result1.get(0);
      //比较版本
      if (AppUpdateUtils.compareVersion(mCacheModel.AppVersion, mDownAppModel.version) == -1) {
        return true;
      } else {
        return false;
      }
    }

    return false;
  }

  private void getRelam() {
    if (null == mRealm) mRealm = Realm.getDefaultInstance();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    EventBus.getDefault().unregister(this);
    unbinder.unbind();
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void onEventMainThread(AppStoreModel mModel) {
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
        mCacheModel.AppVersion = mDownAppModel.version;
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
        .load(url)
        .setDownloadPath(getActivity().getFilesDir() + "/" + name + ".zip")  //文件保存路径
        .start();   //启动下载
  }

  private static final int IMAGE_PICKER = 200;

  public void showPhoto() {
    Intent intent = new Intent(getActivity(), ImageGridActivity.class);
    startActivityForResult(intent, IMAGE_PICKER);
    //Intent intent_album = new Intent("android.intent.action.GET_CONTENT");
    //intent_album.setType("image/*");
    //startActivityForResult(intent_album, IMAGE_STORE);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
      if (data != null && requestCode == IMAGE_PICKER) {
        ArrayList<ImageItem> images =
            (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
        KLog.e(images.get(0).path);
        Glide.with(getActivity()).load(images.get(0).path).into(mAdd_img);
      }
    }
  }

  private void submit() {
    // validate
    String name = app_name.getText().toString().trim();
    if (TextUtils.isEmpty(name)) {
      Toast.makeText(getContext(), "微应用名字", Toast.LENGTH_SHORT).show();
      return;
    }

    String path = app_path.getText().toString().trim();
    if (TextUtils.isEmpty(path)) {
      Toast.makeText(getContext(), "微应用网址", Toast.LENGTH_SHORT).show();
      return;
    }

    // TODO validate success, do something

  }
}
