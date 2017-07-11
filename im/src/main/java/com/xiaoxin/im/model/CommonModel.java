package com.xiaoxin.im.model;

/**
 * Created by Administrator on 2017/7/11 0011.
 */

public class CommonModel {

  /**
   * code : 1
   * succeed : succeed
   * content : {"msg":"存入成功"}
   */

  public String code;
  public String succeed;
  public ContentEntity content;

  public static class ContentEntity {
    /**
     * msg : 存入成功
     */

    public String msg;
  }
}
