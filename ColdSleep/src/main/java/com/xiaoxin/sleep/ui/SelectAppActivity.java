package com.xiaoxin.sleep.ui;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.xiaoxin.sleep.R;
import com.xiaoxin.sleep.adapter.ViewPageAdapter;
import java.util.ArrayList;
import java.util.List;

public class SelectAppActivity extends AppCompatActivity {

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
  }

  private void initTab() {
    mTabLayout.addTab(mTabLayout.newTab().setText("第三方应用"));
    mTabLayout.addTab(mTabLayout.newTab().setText("系统应用"));
    list.add(new AppListFragment());
    list.add(new AppListFragment());
    //ViewPager的适配器
    adapter = new ViewPageAdapter(getSupportFragmentManager(), arr, list);
    mViewpage.setAdapter(adapter);
    //绑定
    mTabLayout.setupWithViewPager(mViewpage);

  }
}
