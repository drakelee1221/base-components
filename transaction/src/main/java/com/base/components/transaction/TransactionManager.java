/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * TransactionManager
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-07 10:41
 */
public abstract class TransactionManager {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final ThreadLocal<TransactionTriggerStrategy> TRIGGER_STRATEGY = new ThreadLocal<>();

  /**
   * 判断当前是否开起Spring事务
   * @return -
   */
  public static boolean isSynchronizationActive(){
    return TransactionSynchronizationManager.isSynchronizationActive();
  }

  /**
   * 添加事务事件
   * @param event -
   */
  public static void addTransactionEvent(TransactionEvent event){
    if(event != null){
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        private TransactionEvent targetEvent = event;
        @Override
        public void afterCommit() {
          targetEvent.afterCommit();
        }
        @Override
        public void afterCompletion(int status) {
          if(status == TransactionSynchronization.STATUS_ROLLED_BACK){
            targetEvent.afterRollback();
          }
          targetEvent.afterCompletion(status);
        }
      });
    }
  }

  /**
   * 设置当前线程事务处理策略
   * @param strategy 处理策略
   */
  public static void setTransactionTriggerStrategy(TransactionTriggerStrategy strategy){
    TRIGGER_STRATEGY.set(strategy);
  }

  /**
   * <pre>
   *   主线程在事务提交或回滚后触发自定义事件策略；
   *   需要在 {@link AbstractPlatformTransactionManager#doCommit(DefaultTransactionStatus)}
   *   或     {@link AbstractPlatformTransactionManager#doRollback(DefaultTransactionStatus)}
   *   之后执行，若当前线程中有 TransactionTriggerStrategy 策略（提前调用{@link #setTransactionTriggerStrategy}设置），则执行策略
   * </pre>
   * 
   * @param status 事务状态
   */
  private static void triggerStrategyAfterCommitOrRollback(DefaultTransactionStatus transactionStatus, int status){
    try {
      TransactionTriggerStrategy transactionTriggerStrategy = TRIGGER_STRATEGY.get();
      if(transactionTriggerStrategy != null && isSynchronizationActive()){
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        transactionTriggerStrategy.doStrategy(synchronizations, transactionStatus, status);
      }
    } finally {
      TRIGGER_STRATEGY.remove();
    }
  }

  /**
   * 触发commit后事件，同{@link AbstractPlatformTransactionManager#triggerAfterCommit(DefaultTransactionStatus)}
   * @param synchronizations 事件集合
   * @param status 事务状态对象
   */
  public static void triggerAfterCommit(List<TransactionSynchronization> synchronizations,
                                        DefaultTransactionStatus status){
    if (status.isNewSynchronization()) {
      if (status.isDebug()) {
        logger.trace("Triggering afterCommit synchronization");
      }
      invokeAfterCommit(synchronizations);
    }
  }

  /**
   * 触发commit后事件，同{@link AbstractPlatformTransactionManager#triggerAfterCompletion(DefaultTransactionStatus, int)}
   * @param synchronizations 事件集合
   * @param status 事务状态对象
   * @param completionStatus 完成状态
   * <p> see:
   * <p> {@link TransactionSynchronization#STATUS_COMMITTED}
   * <p> {@link TransactionSynchronization#STATUS_ROLLED_BACK}
   * <p> {@link TransactionSynchronization#STATUS_UNKNOWN}
   */
  public static void triggerAfterCompletion(List<TransactionSynchronization> synchronizations,
                                            DefaultTransactionStatus status, int completionStatus){
    if (status.isNewTransaction()) {
      if (!status.hasTransaction() || status.isNewTransaction()) {
        if (status.isDebug()) {
          logger.trace("Triggering afterCompletion synchronization");
        }
        // No transaction or new transaction for the current scope ->
        // invoke the afterCompletion callbacks immediately
        invokeAfterCompletion(synchronizations, completionStatus);
      }
      else if (!synchronizations.isEmpty()) {
        // Existing transaction that we participate in, controlled outside
        // of the scope of this Spring transaction manager -> try to register
        // an afterCompletion callback with the existing (JTA) transaction.
        logger.debug("Cannot register Spring after-completion synchronization with existing transaction - " +
                       "processing Spring after-completion callbacks immediately, with outcome status 'unknown'");
        invokeAfterCompletion(synchronizations, TransactionSynchronization.STATUS_UNKNOWN);
      }
    }
  }

  /**
   * 重写{@link AbstractPlatformTransactionManager#doCommit(DefaultTransactionStatus)}时调用
   * @param superDoCommit 调用父类方法
   */
  public static void transactionManagerDoSuperCommit(Runnable superDoCommit,
                                                     DefaultTransactionStatus transactionStatus){
    try {
      superDoCommit.run();
      triggerStrategyAfterCommitOrRollback(transactionStatus, TransactionSynchronization.STATUS_COMMITTED);
    } catch (Exception e) {
      triggerStrategyAfterCommitOrRollback(transactionStatus, TransactionSynchronization.STATUS_UNKNOWN);
      throw e;
    }
  }

  /**
   * 重写{@link AbstractPlatformTransactionManager#doRollback(DefaultTransactionStatus)}时调用
   * @param superDoRollback 调用父类方法
   */
  public static void transactionManagerDoSuperRollback(Runnable superDoRollback,
                                                       DefaultTransactionStatus transactionStatus){
    try {
      superDoRollback.run();
      triggerStrategyAfterCommitOrRollback(transactionStatus, TransactionSynchronization.STATUS_ROLLED_BACK);
    } catch (Exception e) {
      triggerStrategyAfterCommitOrRollback(transactionStatus, TransactionSynchronization.STATUS_UNKNOWN);
      throw e;
    }
  }




  private static void invokeAfterCommit(List<TransactionSynchronization> synchronizations){
    if (synchronizations != null) {
      for (TransactionSynchronization synchronization : synchronizations) {
        synchronization.afterCommit();
      }
    }
  }

  private static void invokeAfterCompletion(List<TransactionSynchronization> synchronizations, int completionStatus){
    if (synchronizations != null) {
      for (TransactionSynchronization synchronization : synchronizations) {
        try {
          synchronization.afterCompletion(completionStatus);
        }
        catch (Throwable e) {
          logger.error("TransactionSynchronization.afterCompletion threw exception", e);
        }
      }
    }
  }







}
