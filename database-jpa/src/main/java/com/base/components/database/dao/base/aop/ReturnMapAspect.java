/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base.aop;

import com.base.components.database.dao.base.annotation.ReturnMapKeys;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 在Jpa的Repository中，使用{@link org.springframework.data.jpa.repository.Query} 注解的方法，
 * 返回{@code  List<Map> 和 Page<Map> } 时，用于更新返回值
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-13 9:15
 */
@Component
@Aspect
@SuppressWarnings("all")
public class ReturnMapAspect {

  @Pointcut("execution(public org.springframework.data.domain.Page<*> *..database.dao.*.*+.*(..) )")
  public void page() {}

  @Pointcut("execution(public java.util.List<*> *..database.dao.*.*+.*(..) )")
  public void list() {}

  @Around("page() || list()")
  public Object changeType(ProceedingJoinPoint joinPoint) throws Throwable {
    Object returnVal = joinPoint.proceed();
    Method method = findMethod(joinPoint);
    if (method == null){
      return returnVal;
    }
    ReturnMapKeys returnMapKeys = AnnotationUtils.findAnnotation(method, ReturnMapKeys.class);
    if(returnMapKeys == null){
      return returnVal;
    }
    if(returnVal instanceof Page){
      Page page = (Page)returnVal;
      if(!page.getContent().isEmpty()){
        return page.map(new Function() {
          @Override
          public Object apply(Object row) {
            return toRow(returnMapKeys.value(), row, returnMapKeys.returnType());
          }
        });
      }
    }else if(returnVal instanceof List){
      List list = (List)returnVal;
      if(!list.isEmpty()){
        List returnList = new ArrayList(list.size());
        for (Object row : list) {
          returnList.add(toRow(returnMapKeys.value(), row, returnMapKeys.returnType()));
        }
        return returnList;
      }
    }
    return returnVal;
  }


  private Method findMethod(JoinPoint joinPoint){
    try {
      return ((MethodSignature)joinPoint.getSignature()).getMethod();
    } catch (Exception e) {
      try {
        Class<?> clazz = Class.forName(joinPoint.getSignature().getDeclaringTypeName());
        return findMethod(clazz, joinPoint.getSignature().getName(),
                          joinPoint.getSignature().toLongString());
      } catch (ClassNotFoundException e1) {
        e.printStackTrace();
        e1.printStackTrace();
      }
    }
    return null;
  }

  private Method findMethod(Class<?> clazz, String methodName, String invokeStr) {
    try {
      Class[] paramClasses = getParamClass(invokeStr);
      return clazz.getMethod(methodName, paramClasses);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private static Class[] getParamClass(String invokeStr) throws ClassNotFoundException {
    String paramStr = invokeStr.substring(invokeStr.indexOf("(") + 1, invokeStr.indexOf(")"));
    String[] paramArr = StringUtils.split(paramStr, ",");
    Class[] paramClasses = new Class[paramArr.length];
    for (int i = 0; i < paramArr.length; i++) {
      paramClasses[i] = Class.forName(paramArr[i]);
    }
    return paramClasses;
  }

  private Object toRow(String[] keys, Object rowObj, Class<?> dtoClass){
    if(!(rowObj instanceof Object[])){
      return rowObj;
    }
    if(Map.class.isAssignableFrom(dtoClass)){
      return toMap(keys, (Object[]) rowObj);
    }else{
      return toDto(keys, (Object[]) rowObj, dtoClass);
    }
  }

  private Object toDto(String[] keys, Object[] array, Class<?> dtoClass){
    try {
      Object row = dtoClass.newInstance();
      for (int i = 0; i < keys.length; i++) {
        String key = keys[i];
        Object val = null;
        if(i < array.length){
          val = array[i];
        }
        BeanUtils.setProperty(row, key, val);
      }
      return row;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object toMap(String[] keys, Object[] array){
    Map<String, Object> row = new LinkedHashMap<>(keys.length);
    for (int i = 0; i < keys.length; i++) {
      String key = keys[i];
      Object val = null;
      if(i < array.length){
        val = array[i];
      }
      row.put(key, val);
    }
    return row;
  }
}
