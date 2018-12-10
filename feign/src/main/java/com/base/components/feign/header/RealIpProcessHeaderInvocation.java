/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.header;

import com.google.common.net.HttpHeaders;
import com.base.components.common.boot.EventHandler;
import com.base.components.common.util.IPUtils;
import com.base.components.common.util.ServletContextHolder;
import org.apache.commons.collections4.map.SingletonMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * 注册动态header获取接口：真实IP传递
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-06 10:26
 */
@Component
public class RealIpProcessHeaderInvocation implements EventHandler<Object, Map<String, String>> {
  private RealIpProcessHeaderInvocation() {
    DynamicHeaderRegistry.registry(this);
  }

  @Override
  public String getId() {
    return RealIpProcessHeaderInvocation.class.getName();
  }

  @Override
  public Map<String, String> onEvent(Object o) {
    HttpServletRequest request = ServletContextHolder.getRequestNullable();
    if(request != null){
      String realIp = IPUtils.getRealIp(request);
      if (StringUtils.isNotBlank(realIp)) {
        return new SingletonMap<>(HttpHeaders.X_FORWARDED_FOR, realIp);
      }
    }
    return Collections.emptyMap();
  }
}
