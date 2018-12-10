package com.base.components.common.constants.msgqueue;

import org.apache.commons.lang3.StringUtils;

/**
 * ChannelSuffix
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-03 10:48
 */
public abstract class ChannelSuffix {

  private static String SUFFIX = "";

  public static void setSuffix(String suffix){
    if(StringUtils.isNotBlank(suffix)){
      SUFFIX = Channel.SPLIT_CHANNEL_SUFFIX + suffix;
    }
  }

  public static String getSuffix(){
    return SUFFIX == null ? "" : SUFFIX;
  }

  public static String clearSuffix(String channel){
    if(StringUtils.isNoneBlank(SUFFIX, channel)){
      return channel.replace(SUFFIX, "");
    }
    return channel;
  }
}
