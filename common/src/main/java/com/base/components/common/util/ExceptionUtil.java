package com.base.components.common.util;

import com.base.components.common.exception.auth.AuthException;

/**
 * 异常辅助类
 *
 * @author <a href="tecyun@foxmail.com">Huangyunyang</a>
 * @version 1.0.0, 2018/3/28 0028 10:40
 */
public class ExceptionUtil {

  public static void hasauth(boolean boo){
    if(!boo){
      throw new AuthException();
    }
  }
}
