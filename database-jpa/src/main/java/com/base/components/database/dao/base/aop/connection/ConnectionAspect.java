/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.database.dao.base.aop.connection;

import com.base.components.transaction.TransactionManager;
import com.base.components.transaction.TransactionTriggerStrategyFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * ConnectionAspect
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-01-12 13:55
 */
@Aspect
@Component
public class ConnectionAspect implements Ordered {
  @Autowired(required = false)
  private TransactionTriggerStrategyFactory transactionTriggerStrategyFactory;

  @Around("execution(* javax.sql.DataSource.getConnection(..))")
  public Object around(ProceedingJoinPoint point) throws Throwable{
    Connection connection = (Connection) point.proceed();
    if(!(connection instanceof ConnectionProxy)){
      if(transactionTriggerStrategyFactory != null){
        TransactionManager.setTransactionTriggerStrategy(
          transactionTriggerStrategyFactory.create(connection)
        );
      }
      return new ConnectionProxy(connection);
    }
    return connection;
  }


  @Override
  public int getOrder() {
    return Ordered.LOWEST_PRECEDENCE;
  }
}
