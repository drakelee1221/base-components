/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn.beans;

import com.codingapi.tx.listener.service.ModelNameService;
import com.lorne.core.framework.utils.encode.MD5Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ModelNameServiceExtImpl
 *
 * @see com.codingapi.tx.springcloud.service.impl.ModelNameServiceImpl
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-14 11:34
 * @since 4.1.0
 */
@Component
@ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
public class ModelNameServiceExtImpl implements ModelNameService {

  @Value("${spring.application.name}")
  private String modelName;

  @Value("${server.port}")
  private Integer port;
  private static String host;
  static {
    try {
      host = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException ignored) {
    }
  }

  @Override
  public String getModelName() {
    return modelName;
  }

  @Override
  public String getUniqueKey() {
    String address = host + port;
    return MD5Util.md5(address.getBytes());
  }

  @Override
  public String getIpAddress() {
    return host + ":" + port;
  }
}
