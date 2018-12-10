/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn.beans;

import com.codingapi.tx.config.service.TxManagerTxUrlService;
import com.base.components.common.util.HttpClientUtil;
import com.base.components.feign.configuration.tx.lcn.LcnTxTransactionConfigurations;
import com.netflix.appinfo.InstanceInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TxManagerTxUrlServiceImpl
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-05-29 17:20
 * @since 4.1.0
 */
@Component
@ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
public class TxManagerTxUrlServiceImpl implements TxManagerTxUrlService {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final AtomicLong lastCheckTime = new AtomicLong(0);
  private final AtomicReference<String> currentTxManagerHost = new AtomicReference<>();
  @Autowired
  private DiscoveryClient discoveryClient;
  @Value("${base.rpc.tx.lcn.txManagerUrlCheckTimeLimit:30000}")
  private long checkTimeLimit = 30_000;
  @Value("${module-tx-manager}")
  private String txManagerServerName;

  @Override
  public String getTxUrl() {
    if(LcnTxTransactionConfigurations.HAS_EXIT.get()){
      throw new RuntimeException("load txManager url error > server is exit");
    }
    //ek中负载遍历一个txManager实例
    try {
      return findTxManagerHost();
    } catch (Exception e) {
      logger.error("load txManager url error", e);
      return null;
    }
  }

  private String findTxManagerHost() {
    long current = System.currentTimeMillis();
    long last = lastCheckTime.get();
    if (current - last > checkTimeLimit) {
      boolean success = lastCheckTime.compareAndSet(last, current);
      if(!success){
        return findTxManagerHost();
      }
      List<ServiceInstance> instances = discoveryClient.getInstances(txManagerServerName);
      ServiceInstance choose = null;
      if(!instances.isEmpty()){
        //随机打乱顺序
        Collections.shuffle(instances);
        for (ServiceInstance serviceInstance : instances) {
          if (serviceInstance instanceof EurekaDiscoveryClient.EurekaServiceInstance) {
            EurekaDiscoveryClient.EurekaServiceInstance instance
              = (EurekaDiscoveryClient.EurekaServiceInstance) serviceInstance;
            if (InstanceInfo.InstanceStatus.UP.equals(instance.getInstanceInfo().getStatus())) {
              try {
                HttpClientUtil.get(instance.getUri() + "/info");
                choose = instance;
                logger.debug("register txManager host > " + instance.getUri());
                break;
              } catch (Exception ignore) {
              }
            }
          }
        }
      }
      if (choose == null) {
        lastCheckTime.set(0L);
      }else{
        currentTxManagerHost.set(choose.getUri().toString());
      }
    }
    String uri = currentTxManagerHost.get();
    logger.debug("currentTxManagerHost = " + uri);
    return StringUtils.isBlank(uri) ? null : (uri + "/tx/manager/");
  }

}
