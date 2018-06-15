package com.xiaoxin.im.common;

import android.text.TextUtils;
import com.tencent.imsdk.TIMUserProfile;
import com.xiaoxin.im.cache.UserDao;

/**
 * Created by dicallc on 2017/6/6 0006.
 */

public class Constant {
  public static final String video_show_url =
      "http://external.live.sinashow.com:2080/cgi-bin/get_new_anchor_list.fcgi?&pid=161&qid=25003&reg_mac=b172b716d0074cf95e10618b9878f30c&user_id=881659527&ver=1.8.3&page=1&0.7672569083024166";
  public static final String image_head_url = "http://img.live.sinashow.com/pic/avatar/";
  public static String baseUrl="http://varietyshop.cn/";
  public static final String app_list = baseUrl+"appstore/loadappList";
  public static final String appOpenUrl = "file:///data/user/0/com.xiaoxin.im/files/unzip/";
  public static final String update_tokie = baseUrl+"qiniu/loadtokie";
  public static final String update_app_model =baseUrl+
      "appstore/PostAppModel";
  public static final String custom_app_list = baseUrl+"appstore/LoadAllApp";
  public static final String delete_custom_app = baseUrl+"appstore/deleteApp";

  public static String getOpenUrl(String name) {
    return name + "/www/index.html";
  }

  /**
   * 内存中用户信息
   */
  public static TIMUserProfile sTIMUserProfile;
  //参数
  public static final String platform = "platform";
  public static final String app_ame = "app_ame";
  public static final String img = "img";
  public static final String version_des = "version_des";
  public static final String down_path = "down_path";
  public static final String username = "username";
  public static final String URL_HEAD = "http://www.";
  public static final String QINiu_URL_HEAD = "http://7xnk07.com1.z0.glb.clouddn.com/";
  public static final String H5TypeNomal = "nomal";
  public static final String H5TypeCustom= "custom";

  public static String getNikeName() {
    //如果内存中姓名为空就取数据库中的
    if (null==sTIMUserProfile||TextUtils.isEmpty(sTIMUserProfile.getIdentifier())) {
      return UserDao.getUserEntry();
    }
    return sTIMUserProfile.getIdentifier();
  }
}
