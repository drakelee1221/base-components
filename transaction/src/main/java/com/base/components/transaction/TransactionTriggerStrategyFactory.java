/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.transaction;

import org.springframework.lang.Nullable;

/**
 * 事务全部完成后执行策略的工厂接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-09 16:45
 */
public interface TransactionTriggerStrategyFactory {

  /**
   * 构建执行策略
   * @param delegate 构建需要对象
   * @return -
   */
  @Nullable
  TransactionTriggerStrategy create(Object delegate);
}
