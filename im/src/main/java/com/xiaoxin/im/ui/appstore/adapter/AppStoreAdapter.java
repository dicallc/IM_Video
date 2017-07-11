package com.xiaoxin.im.ui.appstore.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xiaoxin.im.R;
import com.xiaoxin.im.common.Constant;
import com.xiaoxin.im.model.AppStoreModel;
import java.util.List;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public class AppStoreAdapter extends BaseQuickAdapter<AppStoreModel.ContentBean, BaseViewHolder> {

  public AppStoreAdapter(@Nullable List<AppStoreModel.ContentBean> data) {
    super(R.layout.appstore_i_view, data);
  }

  @Override
  protected void convert(BaseViewHolder mBaseViewHolder, AppStoreModel.ContentBean model) {
    ImageView mView = mBaseViewHolder.getView(R.id.item_icon);
    //如果是true代表是自定义微应用需要加前缀
    if (null==model.isCustom) {
      Glide.with(mContext).load(model.img).into(mView);
    } else {
      Glide.with(mContext).load(Constant.QINiu_URL_HEAD+model.img).into(mView);
    }

    mBaseViewHolder.setText(R.id.title, model.title);
  }
}
