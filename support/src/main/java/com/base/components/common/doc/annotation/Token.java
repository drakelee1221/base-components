package com.base.components.common.doc.annotation;

import com.base.components.common.doc.Scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Token - 仅用于文档生成
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-22 11:38
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Token {

  /**
   * 字段名
   */
  String name() default "token";

  /**
   * 描述
   */
  String value() default "the token string value after login";

  boolean require() default true;

  /**
   * token值请求类别, 默认header
   */
  Scope tokenScope() default Scope.HEADER;
}
