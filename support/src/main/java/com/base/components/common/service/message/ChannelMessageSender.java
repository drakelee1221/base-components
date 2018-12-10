package com.base.components.common.service.message;

import com.base.components.common.constants.msgqueue.Channel;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

/**
 * ChannelMessageSender
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-11 16:53
 */
public interface ChannelMessageSender {
  /**
   * 异步发送事件消息，需要手动管理事务，在事务提交前调用此方法
   * @param messageChannel  NonNull     消息Channel
   * @param eventInfoJson   Nullable    业务Json
   */
  void sendMessage(Channel messageChannel, JsonNode eventInfoJson);


  /**
   * 异步发送事件消息，需要手动管理事务，在事务提交前调用此方法
   * @param messageChannel  NonNull     消息Channel
   * @param eventInfoJson   Nullable    业务Json
   * @param remark          Nullable    备注
   */
  void sendMessage(Channel messageChannel, JsonNode eventInfoJson, String remark);


  /**
   * 同步发送事件消息，需要手动管理事务，在事务提交前调用此方法
   * <p>
   *   注意：请确保有同步事件的监听器在监听中，否则当前线程会等待 {xx.common.cache.syncMessageMaxWaitMillis:30000} 毫秒并抛出超时异常
   * </p>
   * @param messageChannel  NonNull     消息Channel
   * @param eventInfoJson   Nullable    业务Json
   *
   * @return 调用 BaseChannelMessageListener.setReturnValue 方法设置的返回值
   */
  <R extends Serializable> R sendSyncMessage(Channel messageChannel, JsonNode eventInfoJson);

  /**
   * 同步发送事件消息，需要手动管理事务，在事务提交前调用此方法
   * <p>
   *   注意：请确保有同步事件的监听器在监听中，否则当前线程会等待 {xx.common.cache.syncMessageMaxWaitMillis:30000} 毫秒并抛出超时异常
   * </p>
   * @param messageChannel  NonNull     消息Channel
   * @param eventInfoJson   Nullable    业务Json
   * @param remark          Nullable    备注
   *
   * @return 调用 BaseChannelMessageListener.setReturnValue 方法设置的返回值
   */
  <R extends Serializable> R sendSyncMessage(Channel messageChannel, JsonNode eventInfoJson, String remark);

}
