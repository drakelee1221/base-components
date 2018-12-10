package com.base.components.cache.msgqueue.service;

import com.base.components.common.constants.msgqueue.Channel;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

/**
 * BaseSyncChannelMessageListener - 异步消息监听处理
 * <p>
 *  多个异步消息监听可以同时注册同一个 Channel 进行并行处理
 * </p>
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-08 11:28
 */
public abstract class BaseAsyncChannelMessageListener extends BaseChannelMessageListener {

  @Override
  protected final Serializable onSyncMessage(Channel messageChannel, JsonNode eventInfoJson, String remark, boolean isSyncSender) throws Exception {
    onAsyncMessage(messageChannel, eventInfoJson, remark, isSyncSender);
    return null;
  }
}
