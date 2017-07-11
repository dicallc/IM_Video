package com.xiaoxin.im.model;

/**
 * Created by Administrator on 2017/7/11 0011.
 */

public class QiNiuTokie {

  /**
   * code : 1
   * succeed : succeed
   * content : {"uploadToken":"xK5ZiI5Nb5dUs9t_g9l21U0aPXZ3isqBlQTsQX6s:NbBasXQjghJzaFVNyVerJ5jDy2c=:eyJzY29wZSI6ImlmLXBibCIsImRlYWRsaW5lIjoxNDk5NzM5NjMwfQ=="}
   */

  public String code;
  public String succeed;
  public ContentEntity content;

  public static class ContentEntity {
    /**
     * uploadToken : xK5ZiI5Nb5dUs9t_g9l21U0aPXZ3isqBlQTsQX6s:NbBasXQjghJzaFVNyVerJ5jDy2c=:eyJzY29wZSI6ImlmLXBibCIsImRlYWRsaW5lIjoxNDk5NzM5NjMwfQ==
     */

    public String uploadToken;
  }
}
