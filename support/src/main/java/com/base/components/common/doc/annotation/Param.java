package com.base.components.common.doc.annotation;

import com.base.components.common.doc.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Param
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-21 13:04
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
  /**
   * 字段描述
   */
  String value() default "";

  /**
   * 字段名
   */
  String name() default "";

  /**
   * 字段类型，如为动态 Json 对象，则使用 {@link com.base.components.common.doc.type.ObjectType}，
   * 如为动态集合对象，则使用 {@link com.base.components.common.doc.type.ArrayType}
   */
  Class<?> dataType() default String.class;


  /**
   * 字段泛型类, 只有在 dataType = {@link com.base.components.common.doc.type.ArrayType} 时才会有效
   */
  Class<?> genericType() default Object.class;

  /**
   * 是否必填
   */
  boolean required() default false;

  /**
   * 是否在文档中隐藏
   */
  boolean hidden() default false;

  /**
   * 示例
   */
  String example() default "";

  /**
   * 请求参数的类别
   */
  Scope requestScope() default Scope.QUERY;

}
