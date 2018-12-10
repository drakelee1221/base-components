package com.base.components.common.util;

import java.util.List;
import java.util.Map;

/**
 * 隐藏字符
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-12-21 12:45
 */
public class StringHider {


  public static List<Map<String, Object>> hideUserName(List<Map<String, Object>> list, String nameKey) {
    for (Map<String, Object> row : list) {
      Object o = row.get(nameKey);
      String n = "*";
      if (o != null) {
        String name = o.toString();
        if (name.length() > 1) {
          n = name.substring(0, 1) + "*";
        }
        if (name.length() > 2) {
          n += name.substring(name.length() - 1, name.length());
        }
      }
      row.put(nameKey, n);
    }
    return list;
  }

  public static String hidePhone(String phone) {
    if (!ValidatorUtil.isPhoneNum(phone)) {
      throw new IllegalArgumentException("非手机号！");
    }
    return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
  }

  /**
   * 隐藏手机号
   * @param phone -
   * @param flag 标志是否隐藏
   * @return
   */
  public static String hidePhone(String phone, boolean flag) {
    if (flag && phone != null) {
      return hidePhone(phone);
    }
    return phone;
  }

  public static void main(String[] args) {
    System.out.println(StringHider.hidePhone("18523170845"));
  }
}
