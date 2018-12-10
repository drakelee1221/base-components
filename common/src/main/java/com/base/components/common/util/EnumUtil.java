/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * EnumUtil
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-17 11:11
 */
@SuppressWarnings("all")
public abstract class EnumUtil {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * 根据枚举成员属性字段，获取实例
   * @param enumClass 枚举类
   * @param fieldName 成员属性，需要有get方法
   * @param feildValue 成员属性值
   *
   * @return -
   */
  public static <T extends Enum> T parse(Class<T> enumClass, String fieldName, Object feildValue){
    try {
      boolean isNull = feildValue == null;
      Method valuesMethod = enumClass.getMethod("values");
//      Field field = enumClass.getDeclaredField(fieldName);
//      field.setAccessible(true);
      Method get = enumClass.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
      T[] items = (T[]) valuesMethod.invoke(null);
      for (T item : items) {
//        Object val = field.get(item);
        Object val = get.invoke(item);
        if(isNull && val == null){
          return item;
        }
        if(val.equals(feildValue)){
          return item;
        }
      }
    } catch (Exception e) {
      logger.error("", e);
    }
    throw new IllegalArgumentException("类型转换错误");
  }
}
