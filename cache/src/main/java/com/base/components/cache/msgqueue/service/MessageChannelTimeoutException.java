
package com.base.components.cache.msgqueue.service;

/**
 * MessageChannelException
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-05-21 9:27
 */
class MessageChannelTimeoutException extends MessageChannelException {
  private static final long serialVersionUID = 5996604879031998152L;

  MessageChannelTimeoutException() {
  }

  MessageChannelTimeoutException(String message) {
    super(message);
  }

  MessageChannelTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  MessageChannelTimeoutException(Throwable cause) {
    super(cause);
  }

  MessageChannelTimeoutException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
