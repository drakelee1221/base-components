/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.transaction;


import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.List;

/**
 * 事务执行完毕回调策略，结合 TransactionLocal 一起用
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-06 15:08
 */
public interface TransactionTriggerStrategy {

  /**
   * 执行自定义策略，
   * 在{@link TransactionManager#triggerStrategyAfterCommitOrRollback(DefaultTransactionStatus, int)}中执行
   * @param synchronizations 事务事件集合
   * @param transactionStatus 事务状态对象
   * @param status 事务状态，见{@link TransactionSynchronization}
   */
  void doStrategy(List<TransactionSynchronization> synchronizations,
                  DefaultTransactionStatus transactionStatus, int status);
}
