package com.xiaoxin.im.ui.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.socks.library.KLog;
import com.tencent.qcloud.ui.TemplateTitle;
import com.xiaoxin.im.R;
import com.xiaoxin.im.adapter.VideoShowAdapter;
import com.xiaoxin.im.common.onConnectionFinishLinstener;
import com.xiaoxin.im.model.VideoShowModel;
import com.xiaoxin.im.ui.customview.DividerGridItemDecoration;
import com.xiaoxin.im.ui.video.dao.VideoShowDao;
import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends Activity {

  @BindView(R.id.video_show_title) TemplateTitle mTitle;
  @BindView(R.id.video_list) RecyclerView mVideoList;

  List<VideoShowModel.InfoEntity> mlist = new ArrayList<>();
  private VideoShowAdapter mVideoShowAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video_list);
    ButterKnife.bind(this);
    initView();
    initData();
  }

  private void initView() {
    mVideoList.setLayoutManager(new GridLayoutManager(VideoListActivity.this, 3));
    mVideoList.addItemDecoration(new DividerGridItemDecoration(VideoListActivity.this));
    mVideoShowAdapter = new VideoShowAdapter(mlist);
    mVideoList.setAdapter(mVideoShowAdapter);
    mVideoShowAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
      @Override public void onItemClick(BaseQuickAdapter mBaseQuickAdapter, View mView, int mI) {
        List<VideoShowModel.InfoEntity> mData = mBaseQuickAdapter.getData();
        VideoShowModel.InfoEntity mInfoEntity = mData.get(mI);
        Intent intent = new Intent(VideoListActivity.this, VideoShowActivity.class);
        intent.putExtra("url",mInfoEntity.url);
        startActivity(intent);
      }
    });
  }

  private void initData() {
    VideoShowDao.getVideoShowList(new onConnectionFinishLinstener() {
      @Override public void onSuccess(int code, Object result) {
        VideoShowModel mVideoShowModel = (VideoShowModel) result;
        List<VideoShowModel.InfoEntity> mInfo = mVideoShowModel.info;
        mlist.addAll(mInfo);
        mVideoShowAdapter.notifyDataSetChanged();
        KLog.e(result);
      }

      @Override public void onFail(int code, String result) {
        KLog.e(result);
      }
    });
  }
}
