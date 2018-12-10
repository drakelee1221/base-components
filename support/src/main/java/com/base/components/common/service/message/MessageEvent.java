package com.base.components.common.service.message;

/**
 * MessageEvent
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-17 11:01
 */
public interface MessageEvent {

  String getId();

  String getEventInfoJson();

  String getRemark();
}
