package org.apache.cordova.engine;

public interface NomalCallBack {
        public void onSuccess(int code, Object result);

        public void onFail(int code, String result);
        public void onFinish(int code, String result);
    }