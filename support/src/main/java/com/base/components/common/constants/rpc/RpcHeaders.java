package com.base.components.common.constants.rpc;

import org.apache.commons.collections4.map.SingletonMap;

import java.util.Map;
import java.util.UUID;

/**
 * RpcHeaders
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-12 16:04
 */
public interface RpcHeaders {

  /**
   * 标记RPC客户端的调用的header - key
   */
  String CLIENT_MARK_KEY = "rpc_client";

  /**
   * 标记重定向方式为外部地址重定向
   */
  String EXTERNAL_REDIRECT_HEADER = "EXTERNAL_REDIRECT";

  /**
   * 标记RPC客户端的调用的header
   */
  Map<String, String> CLIENT_MARK = new SingletonMap<>(
    CLIENT_MARK_KEY, UUID.randomUUID().toString().replaceAll("-", ""));
}
