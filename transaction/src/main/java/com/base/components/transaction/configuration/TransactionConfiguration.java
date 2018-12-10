/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.transaction.configuration;

import com.base.components.transaction.SimplePlatformTransactionManager;
import com.base.components.transaction.TransactionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * TransactionConfiguration
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-07 11:56
 */
@Configuration
public class TransactionConfiguration {

  @Bean("transactionManager")
  @ConditionalOnMissingBean
  @ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "false", matchIfMissing = true)
  public PlatformTransactionManager transactionManager() {
    return new SimplePlatformTransactionManager();
  }

  @Bean("transactionManager")
  @ConditionalOnMissingBean
  @ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
  public PlatformTransactionManager transactionManager2() {
    return new SimplePlatformTransactionManagerExtent();
  }


  public static class SimplePlatformTransactionManagerExtent extends SimplePlatformTransactionManager{
    private static final long serialVersionUID = -1736377114744851557L;
    @Override
    protected void doCommit(DefaultTransactionStatus status) {
      TransactionManager.transactionManagerDoSuperCommit(new Runnable(){
        @Override
        public void run() {
          SimplePlatformTransactionManagerExtent.super.doCommit(status);
        }
      }, status);
    }
    @Override
    protected void doRollback(DefaultTransactionStatus status) {
      TransactionManager.transactionManagerDoSuperRollback(new Runnable() {
        @Override
        public void run() {
          SimplePlatformTransactionManagerExtent.super.doRollback(status);
        }
      }, status);
    }
  }
}
