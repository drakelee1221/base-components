/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.header;

import com.google.common.collect.Maps;
import com.base.components.common.boot.EventHandler;
import com.base.components.common.exception.GlobalExceptionHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * DynamicHeaderRegistry
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-01-16 10:52
 */
public class DynamicHeaderRegistry {
  private static final Map<String, EventHandler<?, Map<String, String>>> INVOCATION_LIST = Maps.newConcurrentMap();
  private static final ThreadLocal<Map<String,String>> MAIN_NEXT_THREAD_HEADERS = new ThreadLocal<>();
  private DynamicHeaderRegistry(){}
  /**
   * 注册header获取调用方法
   * @param event -
   */
  public synchronized static void registry(EventHandler<?, Map<String, String>> event){
    if(EventHandler.check(event)){
      INVOCATION_LIST.put(event.getId(), event);
    }
  }

  /**
   * 为下一次feign调用设置动态header，feign调用后会被清空
   * @param headers -
   */
  public static void addNextFeignClientHeaders(Map<String, String> headers){
    if(headers == null || headers.isEmpty()){
      return;
    }
    Map<String, String> nextHeaders = MAIN_NEXT_THREAD_HEADERS.get();
    if(nextHeaders == null){
      nextHeaders = Maps.newHashMap();
      MAIN_NEXT_THREAD_HEADERS.set(nextHeaders);
    }
    nextHeaders.putAll(headers);
  }

  /**
   * 获取主线程存放的全部header，调用后 MAIN_NEXT_THREAD_HEADERS 会被清空
   * @param invokeStack
   * @return
   */
  public static Map<String, String> getAllHeaders(String invokeStack){
    GlobalExceptionHandler.FEIGN_INVOKE_STACK.remove();
    if(StringUtils.isNotBlank(invokeStack)){
      GlobalExceptionHandler.FEIGN_INVOKE_STACK.set(invokeStack);
    }
    Map<String, String> headers = Maps.newHashMap();
    for (Map.Entry<String, EventHandler<?, Map<String, String>>> entry : INVOCATION_LIST.entrySet()) {
      Map<String, String> map = entry.getValue().onEvent(null);
      if(map != null){
        headers.putAll(map);
      }

    }
    Map<String, String> nextHeaders = MAIN_NEXT_THREAD_HEADERS.get();
    if(nextHeaders != null){
      headers.putAll(nextHeaders);
      MAIN_NEXT_THREAD_HEADERS.remove();
    }
    return headers;
  }
}
