/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.configuration;

import com.base.components.common.service.database.DatabaseExecutor;
import com.base.components.database.service.JpaDatabaseExecutor;
import com.base.components.transaction.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.persistence.EntityManagerFactory;

/**
 * JPA Configuration - 继承该配置类，并使用如下注解
 * <pre>
 *   {@code
 *      @Configuration
        @EnableTransactionManagement
        @EntityScan("com.mj.he800.common.domain")
        @EnableJpaRepositories(
          value = "com.mj.he800.database.dao",
          repositoryBaseClass = GenericJpaDaoImpl.class
        )
 *   }
 * </pre>
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-11-21 11:30
 */
public class JpaConfiguration {
  @Autowired
  private EntityManagerFactory entityManagerFactory;

  @Bean("transactionManager")
  @ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "false", matchIfMissing = true)
  public PlatformTransactionManager transactionManager() {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @Bean("transactionManager")
  @ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
  public PlatformTransactionManager lcnTransactionManager() {
    return new JpaTransactionManagerExtent(entityManagerFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  public DatabaseExecutor jpaDatabaseExecutor(){
    return new JpaDatabaseExecutor();
  }

  public static class JpaTransactionManagerExtent extends JpaTransactionManager{
    private static final long serialVersionUID = 1213795980507178322L;
    public JpaTransactionManagerExtent() {}
    public JpaTransactionManagerExtent(EntityManagerFactory emf) {
      super(emf);
    }
    @Override
    protected void doCommit(DefaultTransactionStatus status) {
      TransactionManager.transactionManagerDoSuperCommit(new Runnable(){
        @Override
        public void run() {
          JpaTransactionManagerExtent.super.doCommit(status);
        }
      }, status);
    }
    @Override
    protected void doRollback(DefaultTransactionStatus status) {
      TransactionManager.transactionManagerDoSuperRollback(new Runnable() {
        @Override
        public void run() {
          JpaTransactionManagerExtent.super.doRollback(status);
        }
      }, status);
    }
  }
}
