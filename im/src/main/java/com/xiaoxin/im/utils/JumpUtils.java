package com.xiaoxin.im.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import com.xiaoxin.im.ui.video.VideoShowActivity;

/**
 * Created by shuyu on 2016/11/11.
 */

public class JumpUtils {

    /**
     * 跳转到视频播放
     *
     * @param activity
     * @param view
     */
    public static void goToVideoPlayer(Activity activity, View view) {
        Intent intent = new Intent(activity, VideoShowActivity.class);
        //intent.putExtra(VideoShowActivity.TRANSITION, true);
        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        //    Pair pair = new Pair<>(view, VideoShowActivity.IMG_TRANSITION);
        //    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
        //            activity, pair);
        //    ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
        //} else {
        //
        //    activity.overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
        //}
        activity.startActivity(intent);
    }

}
