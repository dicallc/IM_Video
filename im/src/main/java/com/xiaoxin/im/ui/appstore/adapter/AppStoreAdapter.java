package com.xiaoxin.im.ui.appstore.adapter;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xiaoxin.im.R;
import com.xiaoxin.im.common.Constant;
import com.xiaoxin.im.model.AppStoreModel;
import com.xiaoxin.im.utils.GlideCircleTransform;

import java.util.List;

/**
 * Created by jiangdikai on 2017/6/28.
 */

public class AppStoreAdapter extends BaseQuickAdapter<AppStoreModel.ContentBean,BaseViewHolder> {

    public AppStoreAdapter( @Nullable List<AppStoreModel.ContentBean> data) {
        super(R.layout.appstore_i_view, data);
    }

    @Override
    protected void convert(BaseViewHolder mBaseViewHolder, AppStoreModel.ContentBean model) {
         ImageView mView = mBaseViewHolder.getView(R.id.item_icon);
//        BitmapImageViewTarget bitmapImageViewTarget = new BitmapImageViewTarget(mView) {
//            @Override
//            protected void setResource(Bitmap resource) {
//                RoundedBitmapDrawable circularBitmapDrawable =
//                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
//                circularBitmapDrawable.setCircular(true);
//                mView.setImageDrawable(circularBitmapDrawable);
//            }
//        };
        Glide.with(mContext).load(model.img).into(mView);
        mBaseViewHolder.setText(R.id.title,model.title);
    }
}
