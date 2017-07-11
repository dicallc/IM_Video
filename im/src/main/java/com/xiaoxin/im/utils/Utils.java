package com.xiaoxin.im.utils;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/7/10 0010.
 */

public class Utils {
  public static boolean isUrl(String url) {
    Pattern pattern = Pattern.compile(
        "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\/])+$");
   return pattern.matcher(url).matches();
  }
}
