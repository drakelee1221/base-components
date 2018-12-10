package com.base.components.common.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 对象复制
 *
 * @author <a href="morse.jiang@foxmail.com">JiangWen</a>
 * @version 1.0.0, 2017-11-28 11:50
 */
public class ObjectCopyUtil {

  /**
   * 不同类型实体类，相同的属性复制
   *
   * @param from 复制来源
   * @param to 结果类型
   * @param <T> 返回结果
   *
   * @return
   *
   * @throws Exception
   */
  public static <T> T copy(Object from, Class<T> to) {
    try {
      T newT = to.newInstance();
      // 获取属性
      BeanInfo sourceBean = sourceBean = Introspector.getBeanInfo(from.getClass(), Object.class);
      PropertyDescriptor[] sourceProperty = sourceBean.getPropertyDescriptors();

      BeanInfo destBean = Introspector.getBeanInfo(newT.getClass(), Object.class);
      PropertyDescriptor[] destProperty = destBean.getPropertyDescriptors();
      for (int i = 0; i < sourceProperty.length; i++) {
        for (int j = 0; j < destProperty.length; j++) {
          String type1 = sourceProperty[i].getPropertyType().toGenericString();
          String type2 = destProperty[j].getPropertyType().toGenericString();
          if (sourceProperty[i].getName().equals(destProperty[j].getName()) && type1.equals(type2)) {
            // 调用source的getter方法和dest的setter方法
            destProperty[j].getWriteMethod().invoke(newT, sourceProperty[i].getReadMethod().invoke(from));
            break;
          }
        }
      }
      return newT;
    } catch (Exception e) {
      throw new RuntimeException("转换异常", e);
    }
  }


  /**
   * 复制相同的属性的值（忽略空值）
   *
   * @param from
   * @param to
   * @param <T>
   *
   * @return
   */
  public static <T> T copy(T from, T to) {
    try {
      // 获取属性
      BeanInfo sourceBean = sourceBean = Introspector.getBeanInfo(from.getClass(), Object.class);
      PropertyDescriptor[] sourceProperty = sourceBean.getPropertyDescriptors();

      BeanInfo destBean = Introspector.getBeanInfo(to.getClass(), Object.class);
      PropertyDescriptor[] destProperty = destBean.getPropertyDescriptors();
      for (int i = 0; i < sourceProperty.length; i++) {
        for (int j = 0; j < destProperty.length; j++) {
          String type1 = sourceProperty[i].getPropertyType().toGenericString();
          String type2 = destProperty[j].getPropertyType().toGenericString();
          if (sourceProperty[i].getName().equals(destProperty[j].getName()) && type1.equals(type2)) {
            // 调用source的getter方法和dest的setter方法
            if (sourceProperty[i].getReadMethod().invoke(from) != null) {
              destProperty[j].getWriteMethod().invoke(to, sourceProperty[i].getReadMethod().invoke(from));
            }
            break;
          }
        }
      }
      return to;
    } catch (Exception e) {
      throw new RuntimeException("转换异常", e);
    }
  }



  /**
   * 深度克隆对象
   *
   * @param srcObj
   *
   * @return
   */
  public static Object depthClone(Object srcObj) {
    Object cloneObj = null;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ObjectOutputStream oo = new ObjectOutputStream(out);
      oo.writeObject(srcObj);
      ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
      ObjectInputStream oi = new ObjectInputStream(in);
      cloneObj = oi.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return cloneObj;
  }


  public static Map<String, Object> convertBeanToMap(Object bean){
    Class type = bean.getClass();
    Map<String, Object> returnMap = new HashMap<String, Object>();
    BeanInfo beanInfo = null;
    try {
      beanInfo = Introspector.getBeanInfo(type);
    } catch (IntrospectionException e) {
      e.printStackTrace();
    }
    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
    for (int i = 0; i < propertyDescriptors.length; i++) {
      PropertyDescriptor descriptor = propertyDescriptors[i];
      String propertyName = descriptor.getName();
      if (!"class".equals(propertyName)) {
        Method readMethod = descriptor.getReadMethod();
        Object result = null;
        try {
          result = readMethod.invoke(bean, new Object[0]);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }
        if (result != null) {
          returnMap.put(propertyName, result);
        } else {
//          returnMap.put(propertyName, "");
        }
      }
    }
    return returnMap;
  }

}
