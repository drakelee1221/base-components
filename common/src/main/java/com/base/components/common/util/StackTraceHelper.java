package com.base.components.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * StackTraceHelper
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-02 11:33
 */
public abstract class StackTraceHelper {
  private static String thisClassName = StackTraceHelper.class.getName();

  public static String getTargetStack(String packageStartWith, String excludePackageStartWith){
    for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
      String className = stackTraceElement.getClassName();
      if(className.startsWith(packageStartWith) && !className.equals(thisClassName)){
        if(StringUtils.isNotBlank(excludePackageStartWith) && className.startsWith(excludePackageStartWith)){
          continue;
        }
        return stackTraceElement.toString();
      }
    }
    return null;
  }
}
