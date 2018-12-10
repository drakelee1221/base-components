/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.configuration.tx.lcn;

import com.codingapi.tx.datasource.relational.LCNConnection;
import com.codingapi.tx.datasource.relational.LCNDBConnection;
import com.codingapi.tx.datasource.relational.LCNStartConnection;
import com.codingapi.tx.framework.task.TaskState;
import com.codingapi.tx.framework.task.TxTask;
import com.base.components.transaction.TransactionManager;
import com.base.components.transaction.TransactionTriggerStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.List;

/**
 * SimpleTransactionAfterCompleteStrategy
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-09 16:38
 * @since 4.1.0
 */
public class LcnTransactionAfterCompleteTriggerStrategy implements TransactionTriggerStrategy {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final ThreadLocal<LcnTransactionAfterCompleteTriggerStrategy> CURRENT = new ThreadLocal<>();

  private List<TransactionSynchronization> synchronizations;
  private Object delegate;
  private DefaultTransactionStatus transactionStatus;

  LcnTransactionAfterCompleteTriggerStrategy(Object delegate) {
    if(delegate instanceof LCNConnection){
      this.delegate = delegate;
    }
  }

  @Override
  public void doStrategy(List<TransactionSynchronization> synchronizations,
                         DefaultTransactionStatus transactionStatus, int status) {
    if(delegate != null){
      if(!synchronizations.isEmpty()){
        //清空当前线程事务中注册的事件，在LcnTxTransactionServiceAspect切面完成后再执行
        TransactionSynchronizationManager.clearSynchronization();
        TransactionSynchronizationManager.initSynchronization();
      }
      this.synchronizations = synchronizations;
      this.transactionStatus = transactionStatus;
      CURRENT.set(this);
    }
  }

  private void afterComplete(){
    if(delegate != null){
      Connection connection = (Connection) delegate;
      int lcnStatus = checkLcnState(connection);
      if (lcnStatus == TaskState.commit.getCode()) {
        TransactionManager.triggerAfterCommit(synchronizations, transactionStatus);
      }
      int status = (lcnStatus == TaskState.commit.getCode() ? TransactionSynchronization.STATUS_COMMITTED
                                                            : (lcnStatus == TaskState.rollback.getCode()
                                                               ? TransactionSynchronization.STATUS_ROLLED_BACK
                                                               : TransactionSynchronization.STATUS_UNKNOWN));
      TransactionManager.triggerAfterCompletion(synchronizations, transactionStatus, status);
      logger.info("done transaction complete tasks > status = " + status + ", lcnStatus = " + lcnStatus);
    }
  }

  private int checkLcnState(Object lcnConnection){
    int state = -1;
    if(lcnConnection != null) {
      if (lcnConnection instanceof LCNDBConnection) {
        LCNDBConnection lcn = (LCNDBConnection) lcnConnection;
        TxTask waitTask = lcn.getWaitTask();
        if (waitTask != null) {
          state = waitTask.getState();
        }
      }
      else if (lcnConnection instanceof LCNStartConnection) {
        LCNStartConnection lcn = (LCNStartConnection) lcnConnection;
        TxTask waitTask = lcn.getWaitTask();
        if (waitTask != null) {
          state = waitTask.getState();
        }
        if (state == TaskState.commit.getCode()) {
          try {
            Field isCompensateField = LCNStartConnection.class.getDeclaredField("isCompensate");
            isCompensateField.setAccessible(true);
            if(Boolean.valueOf(isCompensateField.get(lcn).toString())){
              Field startStateField = LCNStartConnection.class.getDeclaredField("startState");
              startStateField.setAccessible(true);
              state = Integer.valueOf(startStateField.get(lcn).toString());
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }
    return state;
  }

  static void doStrategyAfterComplete(){
    LcnTransactionAfterCompleteTriggerStrategy lcnTransactionAfterCompleteTriggerStrategy = CURRENT.get();
    if(lcnTransactionAfterCompleteTriggerStrategy != null){
      CURRENT.remove();
      lcnTransactionAfterCompleteTriggerStrategy.afterComplete();
    }
  }
}
