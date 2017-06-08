package com.xiaoxin.im.common;

public interface onConnectionFinishLinstener {
        public void onSuccess(int code, Object result);

        public void onFail(int code, String result);
    }