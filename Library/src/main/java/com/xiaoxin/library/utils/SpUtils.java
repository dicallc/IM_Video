//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.xiaoxin.library.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SpUtils {
    private static final String FILE_NAME = "share_date";

    public SpUtils() {
    }

    public static void setParam(Context context, String key, Object object) {
        String type = object.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences("share_date", 0);
        Editor editor = sp.edit();
        if("String".equals(type)) {
            editor.putString(key, (String)object);
        } else if("Integer".equals(type)) {
            editor.putInt(key, ((Integer)object).intValue());
        } else if("Boolean".equals(type)) {
            editor.putBoolean(key, ((Boolean)object).booleanValue());
        } else if("Float".equals(type)) {
            editor.putFloat(key, ((Float)object).floatValue());
        } else if("Long".equals(type)) {
            editor.putLong(key, ((Long)object).longValue());
        }

        editor.commit();
    }

    public static Object getParam(Context context,String key, Object defaultObject) {
//        Context context = App.getContext();
        String type = defaultObject.getClass().getSimpleName();
        SharedPreferences sp = context.getSharedPreferences("share_date", 0);
        return "String".equals(type)?sp.getString(key, (String)defaultObject):("Integer".equals(type)?Integer.valueOf(sp.getInt(key, ((Integer)defaultObject).intValue())):("Boolean".equals(type)?Boolean.valueOf(sp.getBoolean(key, ((Boolean)defaultObject).booleanValue())):("Float".equals(type)?Float.valueOf(sp.getFloat(key, ((Float)defaultObject).floatValue())):("Long".equals(type)?Long.valueOf(sp.getLong(key, ((Long)defaultObject).longValue())):null))));
    }
}
