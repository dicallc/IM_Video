package com.xiaoxin.im.ui.video;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.xiaoxin.im.R;
import com.xiaoxin.im.ui.SimpleFragment;

public class VideoHomeActivity extends FragmentActivity {


    @BindView(android.R.id.tabhost)
    FragmentTabHost mTabHost;

    private int mImageViewArray[] = {R.drawable.tab_video_main, R.mipmap.zhibo_begin, R.drawable.tab_video_im};
    private final Class mFragmentArray[] = {VideoListFragment.class, SimpleFragment.class, SimpleFragment.class};
    private String mTextviewArray[] = {"首页", "publish", "IM"};
    private LayoutInflater mLayoutInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        ButterKnife.bind(this);
        //TinkerPatch.with().fetchPatchUpdate(true);
        mLayoutInflater = LayoutInflater.from(this);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.contentPanel);
        int fragmentCount = mFragmentArray.length;
        for (int i = 0; i < fragmentCount; i++) {
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            mTabHost.addTab(tabSpec, mFragmentArray[i], null);
            mTabHost.getTabWidget().setDividerDrawable(null);
        }
        mTabHost.getTabWidget().getChildTabViewAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"还在努力做中",Toast.LENGTH_SHORT).show();
            }
        });    mTabHost.getTabWidget().getChildTabViewAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Log.e(TAG, "I am on patch onCreate");
    }
    private static final String TAG = "Tinker.MainActivity";
    /**
     * 动态获取tabicon
     * @param index tab index
     * @return
     */
    private View getTabItemView(int index) {
        View view;
        if (index % 2 == 0) {
            view = mLayoutInflater.inflate(R.layout.tab_button1, null);
        } else {
            view = mLayoutInflater.inflate(R.layout.tab_button, null);
        }
        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        icon.setImageResource(mImageViewArray[index]);
        return view;
    }

}
