/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn.beans;

import com.codingapi.tx.config.service.TxManagerTxUrlService;
import com.codingapi.tx.netty.service.NettyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * LCNServerListener
 *
 * @see com.codingapi.tx.springcloud.listener.ServerListener
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-14 11:40
 * @since 4.1.0
 */
@Component
@ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
public class LcnServerListener implements ApplicationListener<WebServerInitializedEvent> {

  @Autowired
  private NettyService nettyService;
  @Autowired
  private TxManagerTxUrlService txManagerTxUrlService;

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    String txUrl = txManagerTxUrlService.getTxUrl();
    if(StringUtils.isBlank(txUrl)){
      throw new RuntimeException("without TxManager instance status is UP !");
    }
    nettyService.start();
  }
}
