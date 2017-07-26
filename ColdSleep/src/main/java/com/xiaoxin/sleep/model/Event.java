package com.xiaoxin.sleep.model;

import android.support.annotation.IntDef;

import com.xiaoxin.library.model.AppInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by Administrator on 2017/7/21 0021.
 */

public class Event {
  public List<AppInfo> list;
  public static final int SUNDAY = 0;
  public static final int MONDAY = 1;
  public static final int TUESDAY = 2;
  public static final int getDisList = 3;
  public static final int THURSDAY = 4;
  public static final int FRIDAY = 5;
  public static final int SATURDAY = 6;

  @IntDef({ SUNDAY, MONDAY, TUESDAY, getDisList, THURSDAY, FRIDAY, SATURDAY })
  @Retention(RetentionPolicy.SOURCE) public @interface WeekDays {
  }

  public Event(@WeekDays int mCurrentDay) {
    currentDay = mCurrentDay;
  }

  public Event(@WeekDays int currentDay,List<AppInfo> list) {
    this.list = list;
    this.currentDay = currentDay;
  }

  @WeekDays int currentDay = SUNDAY;

  @WeekDays public int getCurrentDay() {
    return currentDay;
  }
}
