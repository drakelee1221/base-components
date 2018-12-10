/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn;

import com.base.components.feign.header.DynamicHeaderRegistry;
import com.codingapi.ribbon.loadbalancer.LcnRibbonConfiguration;
import com.codingapi.tx.RequestInterceptorConfiguration;
import com.codingapi.tx.TransactionConfiguration;
import com.codingapi.tx.aop.bean.TxTransactionLocal;
import com.codingapi.tx.datasource.ILCNConnection;
import com.codingapi.tx.datasource.ILCNResource;
import com.codingapi.tx.datasource.relational.LCNTransactionDataSource;
import com.codingapi.tx.netty.service.NettyService;
import com.codingapi.tx.netty.service.TxManagerHttpRequestService;
import com.codingapi.tx.netty.service.impl.NettyControlServiceImpl;
import com.codingapi.tx.netty.service.impl.NettyServiceImpl;
import com.codingapi.tx.springcloud.interceptor.TransactionAspect;
import com.codingapi.tx.springcloud.listener.ServerListener;
import com.codingapi.tx.springcloud.service.impl.ModelNameServiceImpl;
import com.google.common.collect.Maps;
import com.lorne.core.framework.utils.http.HttpUtils;
import com.base.components.common.boot.EventHandler;
import com.base.components.common.boot.endpoint.RestartCondition;
import com.base.components.transaction.TransactionTriggerStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import javax.annotation.PostConstruct;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TxTransactionConfigurations
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-01-16 13:45
 * @since 4.1.0
 */
@Configuration
@ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
@ComponentScan(basePackages = "com.codingapi.**",
  excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
    classes = {
      TransactionConfiguration.class,
      LcnRibbonConfiguration.class,
      ModelNameServiceImpl.class,
      ServerListener.class,
      NettyControlServiceImpl.class,
      LCNTransactionDataSource.class,
      RequestInterceptorConfiguration.class,
      TransactionAspect.class,
      NettyServiceImpl.class}))
public class LcnTxTransactionConfigurations implements DisposableBean {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public static final AtomicBoolean HAS_EXIT = new AtomicBoolean(Boolean.FALSE);

  @Autowired
  private NettyService nettyService;

  @PostConstruct
  public void init() {
    //定义header传输方式
    DynamicHeaderRegistry.registry(new EventHandler<Object, Map<String,String>>() {
      @Override
      public String getId() {
        return "LcnTxTransaction TxGroup";
      }
      @Override
      public Map<String, String> onEvent(Object o) {
        TxTransactionLocal txTransactionLocal = TxTransactionLocal.current();
        String groupId = txTransactionLocal == null ? null : txTransactionLocal.getGroupId();
        logger.info("LCN-SpringCloud TxGroup info -> groupId:"+groupId);
        if (txTransactionLocal != null) {
          Map<String, String> headers = Maps.newHashMap();
          headers.put("tx-group", groupId);
          return headers;
        }
        return Collections.emptyMap();
      }
    });
  }

  @Bean
  public TransactionTriggerStrategyFactory transactionAfterCompleteStrategyFactory(){
    return new LcnTransactionTriggerStrategyFactory();
  }
//  @Bean
//  public NettyControlService nettyControlService(){
//    return new NettyControlServiceExtImpl();
//  }

//  @Bean
//  @ConfigurationProperties(prefix = "spring.datasource.hikari")
//  @SuppressWarnings("unchecked")
//  public DataSource dataSource(DataSourceProperties properties) throws ClassNotFoundException {
//    DataSource dataSource = properties.initializeDataSourceBuilder().type(
//      (Class<? extends DataSource>) Class.forName("com.zaxxer.hikari.HikariDataSource")).build();
//    LCNTransactionDataSourceNoGroup dataSourceProxy = new LCNTransactionDataSourceNoGroup();
//    dataSourceProxy.setDataSource(dataSource);
////  MaxConnections = (CPU核心数 * 2) + effective_spindle_count
//    dataSourceProxy.setMaxCount(10);
//    return dataSourceProxy;
//  }


  @Bean
  public TxManagerHttpRequestService txManagerHttpRequestService(){
    return new TxManagerHttpRequestService() {
      @Override
      public String httpGet(String url) {
        return HttpUtils.get(url);
      }

      @Override
      public String httpPost(String url, String params) {
        return HttpUtils.post(url, params);
      }
    };
  }

  @Override
  public void destroy() throws Exception {
    HAS_EXIT.set(Boolean.TRUE);
    LcnTxTransactionShitCleaner.shitCleaner(nettyService);
  }

  @Bean
  public ILCNConnection lcnConnection(){
    /**
     * 不加入事务组 > （是否获取旧连接的条件：同一个模块下被多次调用时第一次的事务操作）
     */
    return new LCNTransactionDataSource(){
      @Override
      protected ILCNResource loadConnection() {
        return null;
      }

      @Override
      public boolean hasGroup(String group) {
        return false;
      }
    };
  }

  @Bean
  public RestartCondition lcnRestartCondition(){
    return new RestartCondition() {
      @Override
      public boolean apply() {
        return false;
      }

      @Override
      public String reason() {
        return "Can Not Restart ! this server is a TX LCN client !";
      }
    };
  }
}
