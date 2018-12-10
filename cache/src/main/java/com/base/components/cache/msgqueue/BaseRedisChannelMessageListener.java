package com.base.components.cache.msgqueue;

import com.base.components.cache.redis.DefaultRedisCacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.io.Serializable;

/**
 * 实现redis消息队列传递的解析
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-14 17:06
 */
public abstract class BaseRedisChannelMessageListener implements ChannelMessageListener {

  private RedisSerializer channelSerializer = DefaultRedisCacheManager.REDIS_KEY_SERIALIZER;

  private RedisSerializer messageBodySerializer = DefaultRedisCacheManager.REDIS_VALUE_SERIALIZER;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    if(message != null && message.getBody() != null && message.getChannel() != null){
      String c = (String) channelSerializer.deserialize(message.getChannel());
      onMessage((Serializable) messageBodySerializer.deserialize(message.getBody()), c);
    }
  }

  /**
   * 接收消息
   * @param messageContent 消息体
   * @param messageChannel 消息Channel
   */
  protected abstract void onMessage(Serializable messageContent, String messageChannel);
}
