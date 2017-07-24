package com.xiaoxin.sleep.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xiaoxin.library.model.AppInfo;
import com.xiaoxin.sleep.R;
import java.util.List;

/**
 * Created by Administrator on 2017/7/12 0012.
 */

public class AppListAdapter extends BaseQuickAdapter<AppInfo, BaseViewHolder> {
  public AppListAdapter(@Nullable List<AppInfo> data) {
    super(R.layout.app_dislist_i_view, data);
  }

  @Override protected void convert(BaseViewHolder mBaseViewHolder, AppInfo mAppInfo) {
    //if (mAppInfo.isSelect){
    //  mBaseViewHolder.setChecked(R.id.checkBox,true);
    //}else{
    //  mBaseViewHolder.setChecked(R.id.checkBox,false);
    //}
    mBaseViewHolder.setText(R.id.app_name, mAppInfo.appName);
    ImageView mView = mBaseViewHolder.getView(R.id.app_icon);
    Glide.with(mContext).load(mAppInfo.file_path).into(mView);
  }
}
