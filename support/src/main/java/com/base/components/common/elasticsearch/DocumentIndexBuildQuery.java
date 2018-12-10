package com.base.components.common.elasticsearch;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义在elastic search 文档类上的用于更新索引的sql，<br>
 * 注意文档类的字段名不要出现这种不区分大小写的相同字段，如：userId 和 userid
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-29 11:19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
@Documented
public @interface DocumentIndexBuildQuery {

  /**
   * 构建索引的原生SQL语句
   */
  String value() default "";

  /**
   * 构建索引的原生SQL语句中，ID字段名
   */
  String idColumn() default "id";

  /**
   * 构建索引的原生SQL语句中，返回值字段名的前缀，字段去掉前缀后需和文档类的字段名相同
   */
  String columnPrefix() default "";

  /**
   * 构建索引的原生SQL语句中，返回值字段去掉前缀后是否需要转驼峰处理
   */
  boolean toCamelCase() default true;

  /**
   * 如需转驼峰处理的间隔字符
   */
  String toCamelCaseSplit() default "_";

}
