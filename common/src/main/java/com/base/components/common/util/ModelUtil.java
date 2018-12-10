package com.base.components.common.util;

import java.util.function.Consumer;

/**
 * @author <a href="tecyun@foxmail.com">Huangyunyang</a>
 * @version 1.0.0, 2018/11/29 0029 10:36
 */
public class ModelUtil {

  public static <T extends Object> void setIfNotNull(T value, Consumer<T> consumer) {
    if (value != null) {
      consumer.accept(value);
    }
  }

  public static void setStringIfNotEmpty(String value, Consumer<String> consumer) {
    if (value != null && value.length() > 0) {
      consumer.accept(value);
    }
  }

}
