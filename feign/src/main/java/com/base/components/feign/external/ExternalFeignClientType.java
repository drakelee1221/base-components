/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.feign.external;

import feign.codec.ErrorDecoder;

/**
 * ExternalFeignClientType 见配置文件 rpc-xxx.yml
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2018-02-26 16:35
 */
public enum ExternalFeignClientType {

  DOTNET_COMMON("dotnet-common", ExternalDotnetErrorDecoderHandler.getInstance()),

  DOTNET_USER("dotnet-user", ExternalDotnetErrorDecoderHandler.getInstance())

  ;

  /** 对应 base.rpc.feign.external-services */
  private String type;

  private ErrorDecoder errorDecoder;

  ExternalFeignClientType(String type, ErrorDecoder errorDecoder) {
    this.type = type;
    this.errorDecoder = errorDecoder;
  }

  public String getType() {
    return type;
  }

  public ErrorDecoder getErrorDecoder() {
    return errorDecoder;
  }
}
