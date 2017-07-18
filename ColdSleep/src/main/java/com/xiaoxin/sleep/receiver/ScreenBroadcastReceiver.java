package com.xiaoxin.sleep.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.socks.library.KLog;

public class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) { // 开屏
                KLog.e("开屏");
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
                KLog.e("锁屏" );
            }
        }
    }