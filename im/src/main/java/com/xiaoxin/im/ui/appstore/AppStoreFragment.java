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
import android.widget.TextView;
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
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.socks.library.KLog;
import com.tencent.qcloud.ui.TemplateTitle;
import com.xiaoxin.im.R;
import com.xiaoxin.im.cache.AppDao;
import com.xiaoxin.im.common.Constant;
import com.xiaoxin.im.common.onConnectionFinishLinstener;
import com.xiaoxin.im.model.AppCacheModel;
import com.xiaoxin.im.model.AppStoreModel;
import com.xiaoxin.im.model.CustomAppBean;
import com.xiaoxin.im.model.CustomAppModel;
import com.xiaoxin.im.model.QiNiuTokie;
import com.xiaoxin.im.ui.appstore.adapter.AppStoreAdapter;
import com.xiaoxin.im.ui.appstore.dao.AppStoreDao;
import com.xiaoxin.im.ui.appstore.impl.MySchedulerListener;
import com.xiaoxin.im.ui.appstore.ui.AppWebViewActivity;
import com.xiaoxin.im.ui.appstore.ui.H5WebViewActivity;
import com.xiaoxin.im.ui.customview.DividerGridItemDecoration;
import com.xiaoxin.im.utils.AppUpdateUtils;
import com.xiaoxin.im.utils.ToastUtils;
import com.xiaoxin.im.utils.Utils;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

public class AppStoreFragment extends Fragment {
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";
  @BindView(R.id.appstore_title) TemplateTitle mAppstoreTitle;
  @BindView(R.id.app_list) RecyclerView mAppList;
  Unbinder unbinder;

  List<AppStoreModel.ContentBean> mlist = new ArrayList<>();
  List<AppStoreModel.ContentBean> mCustomlist = new ArrayList<>();
  @BindView(R.id.custom_title) TextView mCustomTitle;
  @BindView(R.id.custom_app_list) RecyclerView mCustomAppList;

  private String mParam1;
  private String mParam2;
  private AppStoreAdapter NorMalAppadapter;
  private String mAppTitle;
  private AppStoreModel.ContentBean mDownAppModel;
  private Realm mRealm;
  private Uri fileUri;
  private EditText app_name;
  private TextInputLayout name_ly;
  private EditText app_path;
  private TextInputLayout app_path_ly;
  private CheckBox mCheck_update;
  private ImageView mAdd_img;
  private CheckBox mCheck_fast_run;
  private EditText app_des;
  private String mSelectIconPath;
  private String mToken;
  private RealmResults<CustomAppModel> mCustomAppListsFromDb;
  private AppStoreAdapter CustomAppAdapter;
  private int mDbCustomAppSize;
  private Intent.ShortcutIconResource mIconRes;
  private MaterialDialog mDialog;

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
    loadUpdateTokie();
    loadCustomAppList();
    loadNormalApp();
  }

  private void loadCustomAppList() {
    loadCustomAppListFromDB();
  }

  private void loadCustomAppListFromDB() {
    getRelam();
    mRealm.executeTransactionAsync(new Realm.Transaction() {

      @Override public void execute(Realm realm) {
        mCustomAppListsFromDb = realm.where(CustomAppModel.class).findAll();
        mDbCustomAppSize = mCustomAppListsFromDb.size();
        if (mCustomAppListsFromDb.size() > 0) {
          DbCustomAppDataToCache(mCustomAppListsFromDb);
          RefreshCustomAppView();
        }

        loadCustomAppListFromNet();
      }
    });
  }

  private void loadCustomAppListFromNet() {
    AppStoreDao.loadCustomAppList(new onConnectionFinishLinstener() {
      @Override public void onSuccess(int code, Object result) {
        CustomAppBean mCustomAppBean = (CustomAppBean) result;
        List<CustomAppBean.ContentEntity.MsgEntity> mMsg = mCustomAppBean.content.msg;
        int netCustomAppSize = mMsg.size();
        //如果是服务器的集合长度大就保存到数据库中
        if (netCustomAppSize > mDbCustomAppSize) {
          //服务器数据填充集合
          NetCustomAppDataToCache(mMsg);
          saveCustomListToDb(mMsg);
          RefreshCustomAppView();
        } else if (netCustomAppSize == 0 && mDbCustomAppSize == 0) {
          //如果数据库和网络上数据都为0 刷新内存中数据
          mCustomlist.clear();
          CustomAppAdapter.getData().clear();
          RefreshCustomAppView();
        }
      }

      @Override public void onFail(int code, String result) {

      }
    });
  }

  private void RefreshCustomAppView() {
    getActivity().runOnUiThread(new Runnable() {
      @Override public void run() {
        if (CustomAppAdapter.getData().size() > 0) {
          mCustomAppList.setVisibility(View.VISIBLE);
          mCustomTitle.setVisibility(View.VISIBLE);
          CustomAppAdapter.notifyDataSetChanged();
        } else {
          mCustomAppList.setVisibility(View.GONE);
          mCustomTitle.setVisibility(View.GONE);
        }
      }
    });
  }

  private void DbCustomAppDataToCache(RealmResults<CustomAppModel> mMsg) {
    mCustomlist.clear();
    for (CustomAppModel mModel : mMsg) {
      mCustomlist.add(new AppStoreModel.ContentBean(mModel.mApp_name, mModel.mApp_path, mModel.mKey,
          mModel.mApp_des, "h5", "true"));
    }
  }

  private void NetCustomAppDataToCache(List<CustomAppBean.ContentEntity.MsgEntity> mMsg) {
    mCustomlist.clear();
    for (int i = 0; i < mMsg.size(); i++) {
      CustomAppBean.ContentEntity.MsgEntity mMsgEntity = mMsg.get(i);
      mCustomlist.add(
          new AppStoreModel.ContentBean(mMsgEntity.app_ame, mMsgEntity.down_path, mMsgEntity.img,
              mMsgEntity.version_des, "h5", "true"));
    }
    CustomAppAdapter.notifyDataSetChanged();
  }

  private void saveCustomListToDb(final List<CustomAppBean.ContentEntity.MsgEntity> mMsg) {
    // TODO: 2017/7/11 0011 是否会成功呢
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        for (int i = 0; i < mMsg.size(); i++) {
          CustomAppModel mOrder = realm.createObject(CustomAppModel.class);
          CustomAppBean.ContentEntity.MsgEntity mMsgEntity = mMsg.get(i);
          mOrder.mApp_name = mMsgEntity.app_ame;
          mOrder.mApp_path = mMsgEntity.down_path;
          mOrder.mApp_des = mMsgEntity.version_des;
          mOrder.mKey = mMsgEntity.img;
        }
      }
    });
  }

  private void loadNormalApp() {
    AppStoreDao.getAppList(new onConnectionFinishLinstener() {
      @Override public void onSuccess(int code, Object result) {
        mlist.clear();
        AppStoreModel models = (AppStoreModel) result;
        List<AppStoreModel.ContentBean> content = models.content;
        mlist.addAll(content);
        NorMalAppadapter.notifyDataSetChanged();
      }

      @Override public void onFail(int code, String result) {
        KLog.e(result);
      }
    });
  }

  private void loadUpdateTokie() {
    AppStoreDao.loadUpdateTokie(new onConnectionFinishLinstener() {
      @Override public void onSuccess(int code, Object result) {
        QiNiuTokie mTokie = (QiNiuTokie) result;
        mToken = mTokie.content.uploadToken;
      }

      @Override public void onFail(int code, String result) {
        mToken = null;
      }
    });
  }

  private void initView() {
    mAppstoreTitle.setMoreImgAction(new View.OnClickListener() {
      @Override public void onClick(View v) {
        ShowAddAppDialog();
      }
    });
    initNomalAppView();
    initCustomAppView();
  }

  private void initCustomAppView() {
    mCustomAppList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
    mCustomAppList.addItemDecoration(new DividerGridItemDecoration(getActivity()));
    CustomAppAdapter = new AppStoreAdapter(mCustomlist);
    CustomAppAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
      @Override public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
        ItemClick(mBaseQuickAdapter, mI);
      }
    });
    CustomAppAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int position) {
        showCustomMenuDialog(mCustomlist.get(position));
        return true;
      }
    });
    mCustomAppList.setAdapter(CustomAppAdapter);
  }

  private void showCustomMenuDialog(final AppStoreModel.ContentBean mContentBean) {

    new MaterialDialog.Builder(getActivity()).items(R.array.custion_function)
        .itemsCallback(new MaterialDialog.ListCallback() {
          @Override public void onSelection(MaterialDialog mMaterialDialog, View mView, int mI,
              CharSequence mCharSequence) {
            //添加快捷方式
            if (mI == 0) {
              addShortCut(mContentBean.title, Constant.URL_HEAD + mContentBean.down_path);
              ToastUtils.showShortToast("添加成功");
            } else if (mI == 1) {
              showloadDialog("正在删除中");
              //删除微应用,先把服务器数据删除，再把数据库删除掉，然后获取数据库数据刷新列表
              AppStoreDao.deleteApp(new onConnectionFinishLinstener() {
                @Override public void onSuccess(int code, Object result) {
                  AppDao.deleteCustomApp(mContentBean.title, new onConnectionFinishLinstener() {
                    @Override public void onSuccess(int code, Object result) {
                      getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                          loadCustomAppListFromDB();
                          dismissloadDialog();
                        }
                      });
                    }

                    @Override public void onFail(int code, String result) {
                      KLog.e(result);
                    }
                  });
                }

                @Override public void onFail(int code, String result) {

                }
              }, mContentBean.title, mContentBean.img);
            }
          }
        })
        .show();
  }

  private void initNomalAppView() {
    mAppList.setLayoutManager(new GridLayoutManager(getActivity(), 4));
    mAppList.addItemDecoration(new DividerGridItemDecoration(getActivity()));
    NorMalAppadapter = new AppStoreAdapter(mlist);
    NorMalAppadapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
      @Override public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
        ItemClick(mBaseQuickAdapter, mI);
      }
    });
    mAppList.setAdapter(NorMalAppadapter);
  }

  private void ItemClick(BaseQuickAdapter mBaseQuickAdapter, int mI) {
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

  private void ShowAddAppDialog() {
    View view = View.inflate(getActivity(), R.layout.appstore_add_app, null);
    app_name = (EditText) view.findViewById(R.id.app_name);
    app_des = (EditText) view.findViewById(R.id.app_des);
    mAdd_img = (ImageView) view.findViewById(R.id.add);
    mAdd_img.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        showPhoto();
      }
    });
    name_ly = (TextInputLayout) view.findViewById(R.id.name_ly);
    app_path = (EditText) view.findViewById(R.id.app_path);
    app_path_ly = (TextInputLayout) view.findViewById(R.id.app_path_ly);
    mCheck_update = (CheckBox) view.findViewById(R.id.check_update);
    mCheck_fast_run = (CheckBox) view.findViewById(R.id.check_fast_run);
    new MaterialDialog.Builder(getActivity()).title("微应用添加")
        .customView(view, false)
        .onPositive(new MaterialDialog.SingleButtonCallback() {
          @Override public void onClick(@NonNull MaterialDialog mMaterialDialog,
              @NonNull DialogAction mDialogAction) {
            showloadDialog("正在添加微应用");
            if (TextUtils.isEmpty(app_name.getText().toString().trim())) {
              ToastUtils.showShortToast("微应用名字不能为空");
              return;
            }
            if (TextUtils.isEmpty(app_des.getText().toString().trim())) {
              ToastUtils.showShortToast("微应用描述不能为空");
              return;
            }
            if (TextUtils.isEmpty(mSelectIconPath)) {
              ToastUtils.showShortToast("微应用Icon还没有选择");
              return;
            }
            if (TextUtils.isEmpty(app_path.getText().toString().trim())) {
              ToastUtils.showShortToast("微应用Icon还没有选择");
              return;
            }
            if (!Utils.isUrl(Constant.URL_HEAD + app_path.getText().toString().trim())) {
              ToastUtils.showShortToast("微应用网址格式错误");
              return;
            }
            if (mCheck_fast_run.isChecked()) {
              addShortCut(app_name.getText().toString().trim(),
                  Constant.URL_HEAD + app_path.getText().toString().trim());
            }
            updateImage();
          }

          private void updateImage() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String key = "icon_" + sdf.format(new Date());
            String token = mToken;
            if (null == token) {
              ToastUtils.showShortToast("获取Tokie失败");
              return;
            }
            UploadManager uploadManager = new UploadManager();
            uploadManager.put(mSelectIconPath, key, token, new UpCompletionHandler() {
              @Override public void complete(String key, ResponseInfo info, JSONObject res) {
                if (info.isOK()) {
                  updateAppModel(key);
                  KLog.e("key" + key);
                  KLog.e("Upload Success");
                } else {
                  KLog.e("Upload Fail");
                }
                KLog.e(key + ",\r\n " + info + ",\r\n " + res);
              }
            }, null);
          }
        })
        .positiveText("添加")
        .negativeText("取消")
        .show();
  }

  private void addShortCut(String tName, String url) {
    // 安装的Intent
    Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
    // 快捷名称
    shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, tName);
    // 快捷图标是允许重复
    shortcut.putExtra("duplicate", false);
    Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
    shortcutIntent.putExtra("app_name", tName);
    shortcutIntent.putExtra("url", url);
    shortcutIntent.setClassName("com.xiaoxin.im", "com.xiaoxin.im.SplashActivity");
    shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
    mIconRes = Intent.ShortcutIconResource.fromContext(getActivity(), R.mipmap.ic_launcher);
    shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, mIconRes);

    // 发送广播
    getActivity().sendBroadcast(shortcut);
  }

  private void updateAppModel(final String mKey) {
    final String app_name = this.app_name.getText().toString().trim();
    final String app_des = this.app_des.getText().toString().trim();
    final String app_path = this.app_path.getText().toString().trim();
    AppStoreDao.UpdateApp(new onConnectionFinishLinstener() {
      @Override public void onSuccess(int code, Object result) {
        ToastUtils.showShortToast("上传成功");
        saveCustiomAppModel(app_name, app_des, app_path, mKey);
        loadCustomAppListFromDB();
        dismissloadDialog();
      }

      @Override public void onFail(int code, String result) {
        ToastUtils.showShortToast("上传失败");
      }
    }, app_name, mKey, app_des, Constant.URL_HEAD + app_path);
  }

  private void openH5WebView(AppStoreModel.ContentBean mDownAppModel) {
    Intent mIntent = new Intent(getActivity(), H5WebViewActivity.class);
    mIntent.putExtra("url", Constant.URL_HEAD + mDownAppModel.down_path);
    startActivity(mIntent);
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

  /**
   * 保存自定义微应用
   */
  private void saveCustiomAppModel(final String mApp_name, final String mApp_des,
      final String mApp_path, final String mKey) {
    getRelam();
    mRealm.executeTransaction(new Realm.Transaction() {
      @Override public void execute(Realm realm) {
        CustomAppModel mCacheModel = realm.createObject(CustomAppModel.class);
        mCacheModel.mApp_name = mApp_name;
        mCacheModel.mApp_des = mApp_des;
        mCacheModel.mApp_path = mApp_path;
        mCacheModel.mKey = mKey;
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
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
      if (data != null && requestCode == IMAGE_PICKER) {
        ArrayList<ImageItem> images =
            (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
        mSelectIconPath = images.get(0).path;
        KLog.e(mSelectIconPath);
        Glide.with(getActivity()).load(mSelectIconPath).into(mAdd_img);
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

  protected void showloadDialog(String title) {
    mDialog = new MaterialDialog.Builder(getActivity()).title(title)
        .content("请深呼吸休息一下")
        .progress(true, 0)
        .progressIndeterminateStyle(true)
        .show();
  }

  protected void dismissloadDialog() {
    if (null != mDialog) {
      mDialog.dismiss();
    }
  }
}
