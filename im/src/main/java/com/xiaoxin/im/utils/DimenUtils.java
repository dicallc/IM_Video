package com.xiaoxin.im.utils;

import android.content.Context;
import java.text.DecimalFormat;

public final class DimenUtils {
	public static int sp2px(Context context, float spValue) {
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}

	public static int px2sp(Context context, float pxValue) {
		float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	public static int dip2px(Context context, int dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	public static String FormatFileSize(long size){
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeStr = "";
		String wrongSize = "0B";
		if(size==0){
			return wrongSize;
		}
		if(size<1024){
			fileSizeStr = df.format((double)size)+"B";
		}else if(size<1048576){
			fileSizeStr = df.format((double)size/1024)+"KB";
		}else if(size<1073741824){
			fileSizeStr = df.format((double)size/1048576)+"MB";
		}else {
			fileSizeStr = df.format((double)size/1073741824)+"GB";
		}
		return fileSizeStr;
	}

}
