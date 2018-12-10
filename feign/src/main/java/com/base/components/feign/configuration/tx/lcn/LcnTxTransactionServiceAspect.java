/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn;

import com.codingapi.tx.springcloud.interceptor.TxManagerInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

/**
 * TxTransactionServiceAspect
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-01-17 14:45
 * @since 4.1.0
 */
@Aspect
@Component
@ConditionalOnProperty(name = "base.rpc.tx.lcn.enable", havingValue = "true")
public class LcnTxTransactionServiceAspect implements Ordered {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  public int getOrder() {
    // 将优先级设置为最高，这样around方法就可以包在最外层
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Autowired
  private TxManagerInterceptor txManagerInterceptor;

//  @Autowired
//  private TxManagerInterceptor txManagerInterceptor;
//
//  @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
//  public void pointTx1() {}
//
//  @Pointcut("@annotation(javax.transaction.Transactional)")
//  public void pointTx2() {}
//
//  @Pointcut("execution(* com.mj.he800.task.service.*.*Service.*(..))")
//  public void pointTask() {}
//
//  @Pointcut("execution(* com.mj.he800.pm.service.*.*Service.*(..))")
//  public void pointPm() {}
//
//  @Pointcut("(pointPm() || pointTask())")
//  public void pointService() {}
//
//  @Pointcut("(pointTx1() || pointTx2())")
//  public void pointTx() {}

  @Pointcut("@annotation(com.codingapi.tx.annotation.TxTransaction)")
  public void txTransaction(){}

//  @Around("pointService() && pointTx()")
  @Around("txTransaction()")
  public Object around(ProceedingJoinPoint point) throws Throwable{
    try {
      logger.debug("annotation-TransactionRunning-start---->");
      return txManagerInterceptor.around(point);
    } finally {
      LcnTransactionAfterCompleteTriggerStrategy.doStrategyAfterComplete();
      logger.debug("annotation-TransactionRunning-end---->");
    }
  }
}
