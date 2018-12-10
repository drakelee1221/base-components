package com.base.components.common.doc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequestBodyModel - 生成 RequestBody 动态对象，仅用于文档生成
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-22 10:28
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequestBodyModel {
  String name() default "PayloadModel";

  String description() default "";

  boolean required() default true;

  Param[] value() default {};

  /**
   * 是否为集合类型
   */
  boolean isCollection() default false;
}
