/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.external;

import com.base.components.common.exception.ExternalFeignClientServices;
import com.google.common.collect.ImmutableMap;
import com.base.components.feign.configuration.BaseFeignConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ExternalFeignClientServices
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-27 15:06
 */
@Service
@RefreshScope
@AutoConfigureBefore(BaseFeignConfiguration.class)
public class ExternalFeignClientServicesImpl implements ExternalFeignClientServices {
  @Autowired
  private ExternalFeignClientServicesProperties externalFeignClientServicesProperties;

  @Override
  public String getHost(String externalServiceName){
    return externalFeignClientServicesProperties.getExternalServices().get(externalServiceName);
  }

  @Override
  public Map<String, String> getAllExternalServices() {
    return ImmutableMap.copyOf(externalFeignClientServicesProperties.getExternalServices());
  }

}
