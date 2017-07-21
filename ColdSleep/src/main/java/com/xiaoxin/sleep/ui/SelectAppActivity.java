package com.xiaoxin.sleep.ui;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.xiaoxin.sleep.AppDao;
import com.xiaoxin.sleep.R;
import com.xiaoxin.sleep.adapter.ViewPageAdapter;
import com.xiaoxin.sleep.common.BaseActivity;
import com.xiaoxin.sleep.model.Event;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SelectAppActivity extends BaseActivity {

  @BindView(R.id.tab_layout) TabLayout mTabLayout;
  @BindView(R.id.viewpage) ViewPager mViewpage;
  @BindView(R.id.toolbar) Toolbar mToolbar;
  @BindView(R.id.appbar) AppBarLayout mAppbar;
  private List<Fragment> list = new ArrayList<>();
  private ViewPageAdapter adapter;

  private String arr[] = { "App", "系统" };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_select_app);
    ButterKnife.bind(this);
    initTab();
    mToolbar.inflateMenu(R.menu.menu_select);
    initData();
  }

  private void initData() {
    showloadDialog("获取app中");
    AppDao.getInstance().initListData(SelectAppActivity.this);
  }

  private void initTab() {
    mTabLayout.addTab(mTabLayout.newTab().setText("第三方应用"));
    mTabLayout.addTab(mTabLayout.newTab().setText("系统应用"));
    list.add(new AppListFragment());
    list.add(new SystemAppFragment());
    //ViewPager的适配器
    adapter = new ViewPageAdapter(getSupportFragmentManager(), arr, list);
    mViewpage.setAdapter(adapter);
    //绑定
    mTabLayout.setupWithViewPager(mViewpage);
  }

  @Subscribe(threadMode = ThreadMode.MAIN) public void onAppDaoMessage(Event msg) {
    switch (msg.getCurrentDay()) {
      case Event.MONDAY: {
        goneloadDialog();
        break;
      }
    }
  }
}
