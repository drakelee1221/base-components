package com.base.components.common.util;

import com.google.common.collect.Lists;
import com.base.components.common.exception.business.BusinessException;

import java.lang.reflect.Field;
import java.util.List;

/**
 * ObjectSetPropsUtil
 *
 * @author <a href="morse.jiang@foxmail.com">JiangWen</a>
 * @version 1.0.0, 2018/3/20 0020 16:32
 */
public class ObjectSetPropsUtil {

  /**
   * 设置对象属性值为空
   *
   * @param t 对象
   * @param ignoreCase 是否忽略属性名大小写
   * @param props 需要设置为空的属性名
   * @param <T>
   *
   * @return
   *
   * @throws Exception
   */
  public static <T> T setPropsNull(T t, Boolean ignoreCase, String... props) {
    try {
      if (props.length > 0) {
        Class c = t.getClass();
        Field[] fields = c.getDeclaredFields();
        for (String prop : props) {
          for (Field field : fields) {
            if (ignoreCase) {
              if (field.getName().equalsIgnoreCase(prop)) {
                field.setAccessible(true);
                field.set(t, null);
                break;
              }
            } else {
              if (field.getName().equals(prop)) {
                field.setAccessible(true);
                field.set(t, null);
                break;
              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessException("实体类转换异常");
    }
    return t;
  }

  /**
   * 设置对象属性值不为空,其余全为空
   *
   * @param t
   * @param ignoreCase
   * @param props
   * @param <T>
   *
   * @return
   */
  public static <T> T setOtherPropsNull(T t, Boolean ignoreCase, String... props) {
    try {
      if (props.length > 0) {
        Class c = t.getClass();
        Field[] fields = c.getDeclaredFields();
        List<Field> res = Lists.newArrayList();
        for (Field field : fields) {
          if ("serialVersionUID".equalsIgnoreCase(field.getName())) {
            continue;
          }
          boolean exist = false;
          for (String prop : props) {
            if (ignoreCase) {
              if (field.getName().equalsIgnoreCase(prop)) {
                exist = true;
              }
            } else {
              if (field.getName().equals(prop)) {
                exist = true;
              }
            }
          }
          if(!exist){
            field.setAccessible(true);
            field.set(t, null);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new BusinessException("实体类转换异常",e);
    }
    return t;
  }



}
