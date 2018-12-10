/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * 在Jpa的Repository中，使用{@link org.springframework.data.jpa.repository.Query} 注解的方法，
 * 返回{@code  List<T> 和 Page<T> } 时使用
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-13 10:20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Inherited
@Documented
public @interface ReturnMapKeys {

  /**
   * 返回值中，每行map中的key
   */
  String[] value() default {};

  /** 指定返回至泛型类型 */
  Class<?> returnType() default Map.class;
}
