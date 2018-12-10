package com.base.components.cache.msgqueue;

import com.base.components.common.constants.msgqueue.Channel;
import org.springframework.data.redis.connection.MessageListener;

/**
 * channel message 监听接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-14 16:41
 */
public interface ChannelMessageListener extends MessageListener {

  /**
   * 返回注册需要监听的channel
   * @return channel
   */
  Channel[] registerChannels();

}
