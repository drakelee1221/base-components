package com.base.components.common.token;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在需要登录的接口Controller上标明必须要验证的登录类型
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-23 10:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Inherited
@Documented
public @interface RequireToken {

  /**
   * 需要验证的登录对象类型，不能为空
   */
  Class<? extends TokenCacheObj>[] value();
}
