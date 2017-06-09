package com.xiaoxin.im.ui.video;

import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.transition.Transition;
import android.view.View;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.shuyu.gsyvideoplayer.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.xiaoxin.im.R;
import com.xiaoxin.im.model.SwitchVideoModel;
import com.xiaoxin.im.ui.video.adapter.MainFragmentPageAdapter;
import com.xiaoxin.im.ui.video.customview.SampleVideo;
import com.xiaoxin.im.ui.video.impl.MyOnPageChangeListener;
import com.xiaoxin.im.ui.video.impl.OnTransitionListener;
import java.util.ArrayList;
import java.util.List;

public class VideoShowActivity extends FragmentActivity {
  public final static String IMG_TRANSITION = "IMG_TRANSITION";
  public final static String TRANSITION = "TRANSITION";

  @BindView(R.id.video_player) SampleVideo videoPlayer;

  OrientationUtils orientationUtils;
  @BindView(R.id.view_pager) ViewPager mViewPager;

  private boolean isTransition;

  private Transition transition;
  private String mUrl;

  private ArrayList<Fragment> fragments ;
  private ChatFragment chatFragment ;
  private NoFragment noFragment ;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video_show);
    ButterKnife.bind(this);
    mUrl = getIntent().getStringExtra("url");
    init();
  }

  private void init() {
    initFloatView();
    String name = "普通";
    SwitchVideoModel switchVideoModel = new SwitchVideoModel(name, mUrl);
    String name2 = "清晰";
    SwitchVideoModel switchVideoModel2 = new SwitchVideoModel(name2, mUrl);
    List<SwitchVideoModel> list = new ArrayList<>();
    list.add(switchVideoModel);
    list.add(switchVideoModel2);
    videoPlayer.setUp(list, true, "");
    //增加封面
    ImageView imageView = new ImageView(this);
    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    imageView.setImageResource(R.mipmap.xxx1);
    videoPlayer.setThumbImageView(imageView);
    //增加title
    //videoPlayer.getTitleTextView().setVisibility(View.VISIBLE);
    //videoPlayer.getTitleTextView().setText("测试视频");
    //设置返回键
    //videoPlayer.getBackButton().setVisibility(View.VISIBLE);
    //设置旋转
    orientationUtils = new OrientationUtils(this, videoPlayer);
    //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
    //videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
    //  @Override public void onClick(View v) {
    //    orientationUtils.resolveByClick();
    //  }
    //});
    //是否可以滑动调整
    //videoPlayer.setIsTouchWiget(true);
    //设置返回按键功能
    videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        onBackPressed();
      }
    });
    //过渡动画
    initTransition();
  }

  private void initFloatView() {
    fragments = new ArrayList<Fragment>();
    chatFragment = new ChatFragment();
    noFragment = new NoFragment();
    fragments.add(noFragment);
    fragments.add(chatFragment);
    MainFragmentPageAdapter
        myFragmentPagerAdapter = new MainFragmentPageAdapter(getSupportFragmentManager(), fragments);
    mViewPager.setAdapter(myFragmentPagerAdapter);
    mViewPager.setCurrentItem(1);
    mViewPager.addOnPageChangeListener(new MyOnPageChangeListener());
  }

  @Override protected void onPause() {
    super.onPause();
    videoPlayer.onVideoPause();
  }

  @Override protected void onResume() {
    super.onResume();
  }

  @TargetApi(Build.VERSION_CODES.KITKAT) @Override protected void onDestroy() {
    super.onDestroy();
    if (orientationUtils != null) orientationUtils.releaseListener();
  }

  @Override public void onBackPressed() {
    //先返回正常状态
    if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
      videoPlayer.getFullscreenButton().performClick();
      return;
    }
    //释放所有
    videoPlayer.setStandardVideoAllCallBack(null);
    GSYVideoPlayer.releaseAllVideos();
    if (isTransition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      super.onBackPressed();
    } else {
      new Handler().postDelayed(new Runnable() {
        @Override public void run() {
          finish();
          overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        }
      }, 500);
    }
  }

  private void initTransition() {
    if (isTransition && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      postponeEnterTransition();
      ViewCompat.setTransitionName(videoPlayer, IMG_TRANSITION);
      addTransitionListener();
      startPostponedEnterTransition();
    } else {
      videoPlayer.startPlayLogic();
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) private boolean addTransitionListener() {
    transition = getWindow().getSharedElementEnterTransition();
    if (transition != null) {
      transition.addListener(new OnTransitionListener() {
        @Override public void onTransitionEnd(Transition transition) {
          super.onTransitionEnd(transition);
          videoPlayer.startPlayLogic();
          transition.removeListener(this);
        }
      });
      return true;
    }
    return false;
  }
}
