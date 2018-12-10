/*
 * Copyright (c) 2017.  mj.he800.com Inc. All rights reserved.
 */

package com.base.components.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;

public class NonBlankObjectMapper extends ObjectMapper {
  private static final long serialVersionUID = 1L;
  public static final ObjectMapper INSTANCE = new NonBlankObjectMapper();

  public NonBlankObjectMapper() {
    this.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
    this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }
}
