package com.xiaoxin.im.ui.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends Activity {

  @BindView(R.id.video_show_title) TemplateTitle mTitle;
  @BindView(R.id.video_list) RecyclerView mVideoList;

  List<VideoShowModel.InfoEntity> mlist = new ArrayList<>();
  @BindView(R.id.swipe) SwipeRefreshLayout mSwipe;
  private VideoShowAdapter mVideoShowAdapter;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video_list);
    ButterKnife.bind(this);
    initView();
    initData();
  }

  @Override protected void onStart() {
    super.onStart();
    // 不能在onCreate中设置，这个表示当前是刷新状态，如果一进来就是刷新状态，SwipeRefreshLayout会屏蔽掉下拉事件
    //swipeRefreshLayout.setRefreshing(true);

    // 设置颜色属性的时候一定要注意是引用了资源文件还是直接设置16进制的颜色，因为都是int值容易搞混
    // 设置下拉进度的背景颜色，默认就是白色的
    mSwipe.setProgressBackgroundColorSchemeResource(android.R.color.white);
    // 设置下拉进度的主题颜色
    mSwipe.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);

    // 下拉时触发SwipeRefreshLayout的下拉动画，动画完毕之后就会回调这个方法
    mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        // 开始刷新，设置当前为刷新状态
        mSwipe.setRefreshing(true);
        initData();

      }
    });
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
        EventBus.getDefault().post(mInfoEntity);
        Intent intent = new Intent(VideoListActivity.this, VideoShowActivity.class);
        intent.putExtra("url", mInfoEntity.url);
        startActivity(intent);
      }
    });
  }

  private void initData() {
    VideoShowDao.getVideoShowList(new onConnectionFinishLinstener() {
      @Override public void onSuccess(int code, Object result) {
        mlist.clear();
        VideoShowModel mVideoShowModel = (VideoShowModel) result;
        List<VideoShowModel.InfoEntity> mInfo = mVideoShowModel.info;
        mlist.addAll(mInfo);
        mVideoShowAdapter.notifyDataSetChanged();
        KLog.e(result);
        // 这个不能写在外边，不然会直接收起来
        mSwipe .setRefreshing(false);
      }

      @Override public void onFail(int code, String result) {
        KLog.e(result);
      }
    });
  }
}
