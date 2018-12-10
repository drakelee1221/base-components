package com.base.components.cache.configuration;

import com.base.components.common.boot.Profiles;
import com.base.components.common.constants.msgqueue.Channel;
import com.base.components.common.constants.msgqueue.ChannelSuffix;
import com.base.components.common.service.message.SysMessageEventService;
import com.base.components.common.util.IPUtils;
import com.google.common.collect.Lists;
import com.base.components.cache.msgqueue.ChannelMessageListener;
import com.base.components.cache.msgqueue.service.BaseAsyncChannelMessageListener;
import com.base.components.cache.msgqueue.service.BaseSyncChannelMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * redis消息队列监听配置
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-14 13:14
 */
@Configuration
@ConditionalOnProperty({"spring.redis.host", "base.common.cache.enable"})
@ConditionalOnBean(SysMessageEventService.class)
public class RedisMessageQueueConfiguration {
  private static final String LOCAL_MSG_QUEUE = "local-msg-queue";

  @PostConstruct
  public void init() {
    Set<String> p = Profiles.getProfilesSetToUpperCase();
    if (p.contains(LOCAL_MSG_QUEUE.toUpperCase()) || !p.contains(Profiles.PROD.toString())) {
      String localMac = IPUtils.getLocalMACAddress();
      if (localMac != null) {
        ChannelSuffix.setSuffix(localMac);
      }
    }
  }

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                     @Autowired(required = false)
                                                                       List<BaseSyncChannelMessageListener> syncListeners,
                                                                     @Autowired(required = false)
                                                                       List<BaseAsyncChannelMessageListener> asyncListeners) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(redisConnectionFactory);
    List<ChannelMessageListener> listeners = Lists.newArrayList();
    if(syncListeners != null && !syncListeners.isEmpty()){
      listeners.addAll(syncListeners);
    }
    if(asyncListeners != null && !asyncListeners.isEmpty()){
      listeners.addAll(asyncListeners);
    }
    if (asyncListeners != null && !asyncListeners.isEmpty()) {
      for (ChannelMessageListener listener : listeners) {
        Channel[] channels = listener.registerChannels();
        if (channels != null && channels.length > 0) {
          container.addMessageListener(listener, Arrays.stream(channels).map(new Function<Channel, ChannelTopic>() {
            @Override
            public ChannelTopic apply(Channel messageChannel) {
              return new ChannelTopic(messageChannel.getId() + ChannelSuffix.getSuffix());
            }
          }).collect(Collectors.toList()));
        }
      }
    }
    return container;
  }

}
