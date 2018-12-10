/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.external;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ExternalFeignClientServicesProperties
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-03-02 14:34
 */
@Component
@ConfigurationProperties("base.rpc.feign")
@RefreshScope
public class ExternalFeignClientServicesProperties {
  @Autowired
  private Environment environment;
  private Map<String, String> externalServices = Maps.newHashMap();

  @RefreshScope
  public Map<String, String> getExternalServices() {
    return externalServices;
  }

  public void setExternalServices(Map<String, String> externalServices) {
    for (Map.Entry<String, String> entry : externalServices.entrySet()) {
      this.externalServices.put(entry.getKey(), environment.getProperty("base.rpc.feign.external-services." + entry.getKey()));
    }
  }
}
