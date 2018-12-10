/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 工作年限工具
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-04 18:47
 */
public class WorkYearsUtil {

  /**
   * @param src 源字符串
   * @return
   */
  public static int getYears(String src){
    if(StringUtils.isNotBlank(src)){
      String re = src.replaceAll("[^0-9]", "");
      if(!"".equals(re)){
        return Integer.parseInt(re);
      }
    }
    return 0;
  }

}
