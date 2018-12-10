package com.base.components.common.exception;

/**
 * ExceptionViewPathHandler
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-04-08 13:57
 */
public interface ExceptionViewPathHandler {
  /** 500 */
  String internalServerError();

  /** 400 */
  String badRequest();

  /** 401 */
  String unauthorized();

  /** 403 */
  String forbidden();

  /** 404 */
  String notFound();
}
