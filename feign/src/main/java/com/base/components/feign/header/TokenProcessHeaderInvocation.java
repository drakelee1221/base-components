/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.header;

import com.base.components.common.boot.EventHandler;
import com.base.components.common.constants.rpc.RpcHeaders;
import com.base.components.common.token.TokenCacheObj;
import com.base.components.common.token.TokenThreadLocal;
import com.base.components.common.token.TokenTypes;
import com.base.components.common.util.JsonUtils;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 注册动态header获取接口：token传递
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-06 10:26
 */
@Component
public class TokenProcessHeaderInvocation implements EventHandler<Object, Map<String, String>> {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private TokenProcessHeaderInvocation(){
    DynamicHeaderRegistry.registry(this);
  }

  @Override
  public String getId() {
    return TokenProcessHeaderInvocation.class.getName();
  }

  @Override
  public Map<String, String> onEvent(Object o) {
    Map<String, String> headers = buildHeader(TokenThreadLocal.getAllTokenCacheObj());
    headers.putAll(RpcHeaders.CLIENT_MARK);
    return headers;
  }

  private Map<String, String> buildHeader(Map<TokenTypes, TokenCacheObj> tokens) {
    Map<String, String> headers = Maps.newHashMap();
    if (tokens != null && !tokens.isEmpty()) {
      for (Map.Entry<TokenTypes, TokenCacheObj> entry : tokens.entrySet()) {
        if (entry.getValue() != null) {
          try {
            headers.put(entry.getKey().getReceiveJsonKey(),
                        URLEncoder.encode(JsonUtils.mapper.writeValueAsString(entry.getValue()), "UTF-8"));
            headers.put(
              TokenCacheObj.CLASS_RECEIVE_PREFIX_KEY + entry.getKey().getReceiveJsonKey(),
              URLEncoder.encode(entry.getValue().getClass().getName(), "UTF-8"));
          } catch (Exception e) {
            logger.error("缓存对象转成json字符串异常", e);
          }
        }
      }
    }
    return headers;
  }
}
