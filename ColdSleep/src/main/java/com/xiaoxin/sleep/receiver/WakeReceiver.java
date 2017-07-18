package com.xiaoxin.sleep.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.socks.library.KLog;
import com.xiaoxin.sleep.service.StopAppService;

public class WakeReceiver extends BroadcastReceiver {

    private final static String TAG = WakeReceiver.class.getSimpleName();
    private final static int WAKE_SERVICE_ID = -1111;

    /**
     * 灰色保活手段唤醒广播的action
     */
    public final static String GRAY_WAKE_ACTION = "com.xiaoxin.sleep";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (GRAY_WAKE_ACTION.equals(action)) {
            KLog.e(TAG, "wake !! wake !! 又收到了");
            StopAppService.toLiveService(context);
//            Intent wakeIntent = new Intent(context, StopAppService.class);
//            context.startService(wakeIntent);
        }
    }
//
//    /**
//     * 用于其他进程来唤醒UI进程用的Service
//     */
//    public static class WakeNotifyService extends Service {
//
//        @Override
//        public void onCreate() {
//            Log.i(TAG, "WakeNotifyService->onCreate");
//            super.onCreate();
//        }
//
//        @Override
//        public int onStartCommand(Intent intent, int flags, int startId) {
//            Log.i(TAG, "WakeNotifyService->onStartCommand");
//            if (Build.VERSION.SDK_INT < 18) {
//                startForeground(WAKE_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
//            } else {
//                Intent innerIntent = new Intent(this, WakeGrayInnerService.class);
//                startService(innerIntent);
//                startForeground(WAKE_SERVICE_ID, new Notification());
//            }
//            return START_STICKY;
//        }
//
//        @Override
//        public IBinder onBind(Intent intent) {
//            // TODO: Return the communication channel to the service.
//            throw new UnsupportedOperationException("Not yet implemented");
//        }
//
//        @Override
//        public void onDestroy() {
//            Log.i(TAG, "WakeNotifyService->onDestroy");
//            super.onDestroy();
//        }
//    }
//
//    /**
//     * 给 API >= 18 的平台上用的灰色保活手段
//     */
//    public static class WakeGrayInnerService extends Service {
//
//        @Override
//        public void onCreate() {
//            Log.i(TAG, "InnerService -> onCreate");
//            super.onCreate();
//        }
//
//        @Override
//        public int onStartCommand(Intent intent, int flags, int startId) {
//            Log.i(TAG, "InnerService -> onStartCommand");
//            startForeground(WAKE_SERVICE_ID, new Notification());
//            //stopForeground(true);
//            stopSelf();
//            return super.onStartCommand(intent, flags, startId);
//        }
//
//        @Override
//        public IBinder onBind(Intent intent) {
//            // TODO: Return the communication channel to the service.
//            throw new UnsupportedOperationException("Not yet implemented");
//        }
//
//        @Override
//        public void onDestroy() {
//            Log.i(TAG, "InnerService -> onDestroy");
//            super.onDestroy();
//        }
//    }
}
