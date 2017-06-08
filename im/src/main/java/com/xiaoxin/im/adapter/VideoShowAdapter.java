package com.xiaoxin.im.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.xiaoxin.im.R;
import com.xiaoxin.im.common.Constant;
import com.xiaoxin.im.model.VideoShowModel;
import java.util.List;

/**
 * Created by dicallc on 2017/6/6 0006.
 */

public class VideoShowAdapter extends BaseQuickAdapter<VideoShowModel.InfoEntity, BaseViewHolder> {
  public VideoShowAdapter( @Nullable List<VideoShowModel.InfoEntity> data) {
    super(R.layout.vedio_i_showlist, data);
  }

  @Override
  protected void convert(BaseViewHolder mBaseViewHolder, VideoShowModel.InfoEntity model) {
    ImageView mView = mBaseViewHolder.getView(R.id.img_icon);
    Glide.with(mContext).load(Constant.image_head_url+model.id+"_"+model.phid+"_720*720.jpg").into(mView);
    mBaseViewHolder.setText(R.id.video_name,model.familyname);
    mBaseViewHolder.setText(R.id.item_location,model.pos);

  }
}
