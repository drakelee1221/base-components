/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import org.slf4j.LoggerFactory;

/**
 * 日志
 * <p>直接使用该接口的静态{@link #get()}方法，或实现该接口并使用{@link #log()}方法；</p>
 * <p>推荐在需要写日志的类中使用以下代码：</p>
 * <p>{@code
 *   private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
 * }</p>
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-09-06 16:19
 */
public interface Logs {

  /** 实现类调用 */
  default org.slf4j.Logger log(){
    return LoggerFactory.getLogger(getClass());
  }

  /** 静态调用 */
  static org.slf4j.Logger get(){
    return LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
  }
}
