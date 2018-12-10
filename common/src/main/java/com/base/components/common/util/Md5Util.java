/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Md5Util
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-12 15:41
 */
public class Md5Util {

  private Md5Util() {
  }

  public static String md5(String s) {
    return DigestUtils.md5Hex(s);
  }
}
