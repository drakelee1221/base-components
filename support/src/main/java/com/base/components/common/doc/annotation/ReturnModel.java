package com.base.components.common.doc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ReturnModel - 动态返回对象，仅用于文档生成
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-15 15:10
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ReturnModel {

  Param[] value() default {};

  /**
   * 引用的基础数据类型，动态返回对象会根据此类的字段先进行创建
   */
  Class<?> baseModel() default Void.class;

  /**
   * 是否为集合类型
   */
  boolean isCollection() default false;

}
