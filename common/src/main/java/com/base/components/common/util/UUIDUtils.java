package com.base.components.common.util;

import java.util.UUID;

/**
 * @author <a href="420595368@qq.com">hyy</a>
 * @version 1.0.0, 2017-07-03
 */
public class UUIDUtils {

  public static String generateKey() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }
}
