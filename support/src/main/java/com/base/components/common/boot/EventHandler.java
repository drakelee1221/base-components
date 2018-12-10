package com.base.components.common.boot;

import org.springframework.lang.Nullable;

/**
 * EventHandler - 事件接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-24 12:50
 */
public interface EventHandler<T, R> {

  /**
   * 事件唯一ID，注册事件时会根据此ID去重
   *
   * @return - 非空ID
   */
  String getId();

  /**
   * 触发事件
   *
   * @param t -
   *
   * @return -
   */
  @Nullable
  R onEvent(@Nullable T t);

  @SuppressWarnings("all")
  static boolean check(EventHandler event){
    return event != null && event.getId() != null;
  }
}
