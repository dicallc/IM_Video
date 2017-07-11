package com.xiaoxin.im.common;

import com.tencent.imsdk.TIMUserProfile;

/**
 * Created by dicallc on 2017/6/6 0006.
 */

public class Constant {
  public static final String video_show_url="http://external.live.sinashow.com:2080/cgi-bin/get_new_anchor_list.fcgi?&pid=161&qid=25003&reg_mac=b172b716d0074cf95e10618b9878f30c&user_id=881659527&ver=1.8.3&page=1&0.7672569083024166";
  public static final String image_head_url="http://img.live.sinashow.com/pic/avatar/";
  public static final String app_list="http://learnserver.duapp.com/appstore/loadappList";
  public static final String appOpenUrl="file:///data/user/0/com.xiaoxin.im/files/unzip/";
  public static final String update_tokie="http://learnserver.duapp.com/qiniu/loadtokie";
  public static final String update_app_model="http://learnserver.duapp.com/appstore/PostAppModel";
  public static String getOpenUrl(String name){
    return name+ "/www/index.html";
  }

  /**
   * 内存中用户信息
   */
  public static TIMUserProfile sTIMUserProfile;
//参数
  public static final String platform="platform";
  public static final String app_ame="app_ame";
  public static final String img="img";
  public static final String version_des="version_des";
  public static final String down_path="down_path";
  public static final String username="username";
  public static final String URL_HEAD="https://www.";
}
