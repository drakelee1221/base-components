package com.base.components.common.constants.msgqueue;

import com.base.components.common.boot.SpringBootApplicationRunner;
import com.base.components.common.tools.ClassFinder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * Channel - 使用枚举类实现此接口,
 * 实现的枚举类应在包路径下： SpringBootApplicationRunner.getProjectPackagePrefix() + ".common.constants.msgqueue.channels"
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-01 17:02
 */
public interface Channel {

  String SPLIT_CHANNEL = "#";

  String SPLIT_CHANNEL_SUFFIX = "@";

  String SPLIT_HANDLE = "&";

  String SPLIT_HANDLE_SYNC = SPLIT_HANDLE + "SYNC";

  /**
   * 每个 Channel 的枚举对象名
   *
   * @return name
   */
  String name();

  /**
   * 获取描述
   *
   * @return description
   */
  String getDesc();

  /**
   * 每个 Channel 的唯一 ID
   *
   * @return ID
   */
  default String getId() {
    return buildId(this);
  }

  /**
   * 默认构建ID的方法 > name#className
   *
   * @param channel -
   *
   * @return ID
   */
  static String buildId(Channel channel) {
    return channel.name() + SPLIT_CHANNEL + channel.getClass().getName();
  }

  /**
   * 解析 Channel ID 为一个具体对象
   *
   * @param channelId 每个 Channel 的唯一 ID
   * @param <C> -
   *
   * @return Channel
   */
  @SuppressWarnings("unchecked")
  static <C extends Channel> C parse(String channelId) {
    String c = ChannelSuffix.clearSuffix(channelId);
    if (StringUtils.isNotBlank(c)) {
      String[] arr = StringUtils.split(c, SPLIT_CHANNEL);
      if (arr != null && arr.length > 1) {
        try {
          return (C) Enum.valueOf((Class<Enum>) Class.forName(arr[1]), arr[0]);
        } catch (Exception e) {
          throw new IllegalArgumentException(e);
        }
      }
    }
    return null;
  }

  static Set<Channel> getAllChannels() {
    return AllChannels.ALL_CHANNELS;
  }

  class AllChannels {
    private static Set<Channel> ALL_CHANNELS;
    static {
      Class<Channel> channelClass = Channel.class;
      Class<Enum> enumClass = Enum.class;
      String pkg = SpringBootApplicationRunner.getProjectPackagePrefix() + ".common.constants.msgqueue.channels";
      Set<Channel> channels = Sets.newLinkedHashSet();
      try {
        for (Class<?> c : ClassFinder.findWithPackage(pkg)) {
          if (c != channelClass && channelClass.isAssignableFrom(c) && enumClass.isAssignableFrom(c)) {
            Method method = c.getMethod("values");
            method.setAccessible(true);
            Channel[] values = (Channel[]) method.invoke(null);
            channels.addAll(Arrays.asList(values));
          }
        }
      } catch (Exception ignore) {
      }
      ALL_CHANNELS = ImmutableSet.copyOf(channels);
    }
  }
}
