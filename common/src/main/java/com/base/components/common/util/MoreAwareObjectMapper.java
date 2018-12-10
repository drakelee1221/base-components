package com.base.components.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.text.SimpleDateFormat;

/**
 * 
 * <p>File Name: MoreAwareObjectMapper.java</p>
 * <p>Description: ObjectMapper</p>
 * <p>Copyright(c) 2010-2017 mj.com Inc. All Rights Reserved. </p>
 * <p>Other: </p>
 * <p>Date：Dec 3, 2016</p>
 * <p>Modification Record 1: </p>
 * 
 * <pre>
 *    Modified Date：
 *    Version：
 *    Modifier：
 *    Modification Content：
 * </pre>
 * 
 * <p>Modification Record 2：…</p>
 * 
 * @author <a href="drakelee1221@gmail.com">ligeng</a>
 * @version 1.0.0
 */
public class MoreAwareObjectMapper extends ObjectMapper {
  private static final long serialVersionUID = 1L;

  public MoreAwareObjectMapper() {

    // this.registerModule(new GuavaModule());
    // this.registerModule(new JodaModule());

    //BigDecimal 表示方式，true = 原数样式，false = 科学计数
    this.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
//    this.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
    this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }
}
