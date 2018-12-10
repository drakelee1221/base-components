/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.invoke.MethodHandles;

/**
 * 简单事务支持实现
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-07 15:42
 */
public class SimplePlatformTransactionManager extends AbstractPlatformTransactionManager {
  private static final long serialVersionUID = -5447803547017834712L;
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static String RESOURCE_KEY = "SIMPLE_PLATFORM_TRANSACTION_RESOURCE_KEY";

  public SimplePlatformTransactionManager() {
  }

  @Override
  protected Object doGetTransaction() throws TransactionException {
    TransactionObject transaction = new TransactionObject();
    Object resource = TransactionSynchronizationManager.getResource(RESOURCE_KEY);
    transaction.hasResource = (resource != null);
    return transaction;
  }

  @Override
  protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
    logger.info("Simple Transaction > begin");
    TransactionSynchronizationManager.bindResource(RESOURCE_KEY, (byte)0);
  }

  @Override
  protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
    logger.info("Simple Transaction > commit");
  }

  @Override
  protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
    logger.info("Simple Transaction > rollback");
  }

  @Override
  protected boolean isExistingTransaction(Object transaction) throws TransactionException {
    return ((TransactionObject)transaction).hasResource;
  }

  @Override
  protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
    logger.info("Simple Transaction > doSetRollbackOnly");
  }

  @Override
  protected void doCleanupAfterCompletion(Object transaction) {
    TransactionSynchronizationManager.unbindResourceIfPossible(RESOURCE_KEY);
  }

  public static class TransactionObject{
    private boolean hasResource;
  }
}
