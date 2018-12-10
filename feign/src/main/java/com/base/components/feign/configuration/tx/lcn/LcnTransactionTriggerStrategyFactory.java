/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn;

import com.codingapi.tx.datasource.relational.LCNConnection;
import com.base.components.transaction.TransactionTriggerStrategy;
import com.base.components.transaction.TransactionTriggerStrategyFactory;
import org.springframework.lang.Nullable;

/**
 * SimpleTransactionAfterCompleteStrategyFactory
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-09 16:46
 * @since 4.1.0
 */
public class LcnTransactionTriggerStrategyFactory implements TransactionTriggerStrategyFactory {

  @Override
  @Nullable
  public TransactionTriggerStrategy create(Object delegate) {
    if(delegate instanceof LCNConnection){
      return new LcnTransactionAfterCompleteTriggerStrategy(delegate);
    }
    return null;
  }
}
