package com.base.components.common.boot.endpoint;

/**
 * RestartCondition - 用于 CustomRestartEndpoint 判断是否可以重启
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-31 13:29
 */
public interface RestartCondition {

  /**
   * @return true = 可以重启， false = 不能重启
   */
  boolean apply();

  /**
   * @return 原因
   */
  String reason();
}
