package com.base.components.common.constants.msgqueue;

/**
 * SysMessageEvent 标识hasDone 字段
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-15 11:48
 */
public interface MessageChannelStatus {

  /** 执行中，唯一标识 */
  Integer DOING = -1;

  /** 未开始 */
  Integer UNDONE = 0;

  /** 已完成 */
  Integer DONE = 1;

  /** 异常 */
  Integer ERROR = 2;
}
