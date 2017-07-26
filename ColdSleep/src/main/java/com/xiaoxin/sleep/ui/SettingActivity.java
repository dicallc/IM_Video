package com.xiaoxin.sleep.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.allen.library.SuperTextView;
import com.xiaoxin.library.common.LibraryCons;
import com.xiaoxin.sleep.R;
import com.xiaoxin.sleep.view.SettingNormalView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_layout)
    CoordinatorLayout mainLayout;
    @BindView(R.id.setting_select_app)
    SettingNormalView settingSelectApp;
    @BindView(R.id.setting_screen_sleep)
    SuperTextView settingScreenSleep;
    @BindView(R.id.setting_screen_sleep_time)
    SettingNormalView settingScreenSleepTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initToobar();
    }

    private void initToobar() {
        toolbar.setTitle("设置");
        setSupportActionBar(toolbar);
        //关键下面两句话，设置了回退按钮，及点击事件的效果
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @OnClick(R.id.setting_select_app)
    public void toSelectApp() {
        Intent mIntent = new Intent(SettingActivity.this, SelectAppActivity.class);
        mIntent.putExtra(LibraryCons.ACTION, LibraryCons.ACTION_OPEN);
        startActivity(mIntent);
    }
}
