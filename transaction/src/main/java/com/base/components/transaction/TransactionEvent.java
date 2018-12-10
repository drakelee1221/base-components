/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.transaction;

import org.springframework.transaction.support.TransactionSynchronization;

/**
 * TransactionEvent
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-09 15:37
 */
public abstract class TransactionEvent {

  public void afterCommit() {
  }

  public void afterRollback() {
  }

  /**
   * @param status
   * <p> see:
   * <p> {@link TransactionSynchronization#STATUS_COMMITTED}
   * <p> {@link TransactionSynchronization#STATUS_ROLLED_BACK}
   * <p> {@link TransactionSynchronization#STATUS_UNKNOWN}
   */
  public void afterCompletion(int status) {
  }
}
