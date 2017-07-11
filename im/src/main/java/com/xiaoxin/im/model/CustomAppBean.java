package com.xiaoxin.im.model;

import java.util.List;

/**
 * Created by Administrator on 2017/7/11 0011.
 */

public class CustomAppBean {

  /**
   * code : 1
   * succeed : succeed
   * content : {"msg":[{"app_ame":"百度浏览器","down_path":"https://www.baidu.com","img":"img","version_des":"version_des","username":"阿里","_id":"59644be0a7e7ce107d8484b0","__v":0,"create_date":"2017-07-11T03:54:08.582Z"}]}
   */

  public String code;
  public String succeed;
  public ContentEntity content;

  public static class ContentEntity {
    /**
     * msg : [{"app_ame":"百度浏览器","down_path":"https://www.baidu.com","img":"img","version_des":"version_des","username":"阿里","_id":"59644be0a7e7ce107d8484b0","__v":0,"create_date":"2017-07-11T03:54:08.582Z"}]
     */

    public List<MsgEntity> msg;

    public static class MsgEntity {
      /**
       * app_ame : 百度浏览器
       * down_path : https://www.baidu.com
       * img : img
       * version_des : version_des
       * username : 阿里
       * _id : 59644be0a7e7ce107d8484b0
       * __v : 0
       * create_date : 2017-07-11T03:54:08.582Z
       */

      public String app_ame;
      public String down_path;
      public String img;
      public String version_des;
      public String username;
      public String _id;
      public int __v;
      public String create_date;
    }
  }
}
