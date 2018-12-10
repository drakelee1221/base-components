/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import org.springframework.util.Assert;

/**
 * SqlLikeHelper
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-04-11 9:28
 */
public abstract class SqlLikeHelper {
  private static final String MATCHING = "%";

  public static String leftAndRight(String value) {
    Assert.notNull(value, "like text is null");
    return MATCHING + check(value) + MATCHING;
  }
  public static String left(String value) {
    Assert.notNull(value, "like text is null");
    return MATCHING + check(value);
  }
  public static String right(String value) {
    Assert.notNull(value, "like text is null");
    return check(value) + MATCHING;
  }
  public static String none(String value) {
    Assert.notNull(value, "like text is null");
    return check(value);
  }

  private static String check(String value) {
    if (value.startsWith(MATCHING)) {
      value = value.substring(1);
    }
    else if (value.endsWith(MATCHING)) {
      value = value.substring(0, value.length() - 1);
    }
    else {
      return value;
    }
    return check(value);
  }
}
