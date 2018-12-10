package com.base.components.cache.msgqueue.service;

import com.base.components.common.constants.msgqueue.Channel;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * BaseSyncChannelMessageListener - 同步消息监听器
 * <p>
 *  多个同步消息监听器如果注册同一个 Channel，则互相会进行互斥，每次发布只会有其中一个同步监听器获得到处理的权限
 * </p>
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-08 11:28
 */
public abstract class BaseSyncChannelMessageListener extends BaseChannelMessageListener {

  @Override
  protected final void onAsyncMessage(Channel messageChannel, JsonNode eventInfoJson, String remark, boolean isSyncSender) throws Exception {
    onSyncMessage(messageChannel, eventInfoJson, remark, isSyncSender);
  }
}
