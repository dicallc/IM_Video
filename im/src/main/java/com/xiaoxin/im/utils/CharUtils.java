package com.xiaoxin.im.utils;

/**
 * author：Administrator on 2016/12/26 10:32
 * description:文件说明
 * version:版本
 */
public class CharUtils {
  protected static String[] arr = {
      "点亮了红心", "进入房间", "你要死啊？", "主播我爱你，求包养", "姑娘，你的床总是人来人往，那么繁忙", "成熟不是心变老，而是眼泪在眼里打转却还保持微笑",
      "宝贝，别淘气", "我们说好不分离，要一直一直在一起", "在这个利欲熏心冷暖自知的年代，遇见你们是我意想不到的美好", "我们说好不分离，要一直一直在一起",
      "明天太阳依旧升起，转角我们能否相遇?"
  };
  protected static String[] name_arr = {
      "冷安夏","卖身葬楼主","飞吧皮卡丘","老三的老二老大了","烤鸭蛋给家长","小心，这逼有诈","萌面欧巴桑","囧囧有神","点我者得爱情","七尺dà乳","一年级再贱i","此处禁止大小便","按键伤人","郑射你无罪","咬住JJ猛舔不放","煮人为快乐之本","温柔一刀"
  };

  public static String getRandomString(int length) {
    int index=(int)(Math.random()*arr.length);
    //Random random = new Random();
    //StringBuffer sb = new StringBuffer();
    //for (int i = 0; i < length; i++) {
    //  int number = random.nextInt(3);
    //  long result = 0;
    //  switch (number) {
    //    case 0:
    //      result = Math.round(Math.random() * 25 + 65);
    //      sb.append(String.valueOf((char) result));
    //      break;
    //    case 1:
    //      result = Math.round(Math.random() * 25 + 97);
    //      sb.append(String.valueOf((char) result));
    //      break;
    //    case 2:
    //      sb.append(String.valueOf(new Random().nextInt(10)));
    //      break;
    //  }
    //}
    return arr[index];
  }
  public static String getRandomName(int length) {
    int index=(int)(Math.random()*name_arr.length);
    //Random random = new Random();
    //StringBuffer sb = new StringBuffer();
    //for (int i = 0; i < length; i++) {
    //  int number = random.nextInt(3);
    //  long result = 0;
    //  switch (number) {
    //    case 0:
    //      result = Math.round(Math.random() * 25 + 65);
    //      sb.append(String.valueOf((char) result));
    //      break;
    //    case 1:
    //      result = Math.round(Math.random() * 25 + 97);
    //      sb.append(String.valueOf((char) result));
    //      break;
    //    case 2:
    //      sb.append(String.valueOf(new Random().nextInt(10)));
    //      break;
    //  }
    //}
    return name_arr[index];
  }
}
