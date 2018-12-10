
package com.base.components.cache.msgqueue.service;

/**
 * MessageChannelException
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-05-21 9:27
 */
class MessageChannelException extends RuntimeException {
  private static final long serialVersionUID = 5247924906953684460L;

  MessageChannelException() {
  }

  MessageChannelException(String message) {
    super(message);
  }

  MessageChannelException(String message, Throwable cause) {
    super(message, cause);
  }

  MessageChannelException(Throwable cause) {
    super(cause);
  }

  MessageChannelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
