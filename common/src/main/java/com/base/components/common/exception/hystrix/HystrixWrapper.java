/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.exception.hystrix;

import com.base.components.common.exception.ExceptionResponseEntity;

/**
 * HystrixWrapper 包装Hystrix异常的统一接口
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-12 14:36
 *
 */
public interface HystrixWrapper  {

  ExceptionResponseEntity getExceptionResponseEntity();

}
