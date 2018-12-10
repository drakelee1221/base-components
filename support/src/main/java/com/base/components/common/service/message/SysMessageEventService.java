package com.base.components.common.service.message;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * SysMessageEvent Service
 *
 * @author : code generator
 * @version : 1.0
 * @since : 2017-11-15
 */
public interface SysMessageEventService {

  /** 新开事物提交 */
  String createHandle(String id, String listenerId, String listenerClass) throws Exception;

  /** 当前事物提交 */
  MessageEvent findById(String id);

  /** 新开事物提交 */
  void updateDoing(String handleId, String channelId, String error);

  /** 当前事物提交 */
  boolean existsById(String id);

  /** 新开事物提交 */
  boolean existsByIdWithNewTx(String id);

  /** 当前事物提交 */
  MessageEvent save(StackTraceElement stack, String messageChannel, JsonNode eventInfoJson, String remark);

  /** 新开事物提交 */
  MessageEvent saveWithNewTx(StackTraceElement stack, String messageChannel, JsonNode eventInfoJson, String remark);
}