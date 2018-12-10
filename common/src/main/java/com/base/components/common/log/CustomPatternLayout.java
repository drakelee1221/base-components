package com.base.components.common.log;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * CustomPatternLayout
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-03 11:09
 */
public class CustomPatternLayout extends PatternLayout {
  public static final Map<String, String> DEFAULT_CONVERTER_MAP = new HashMap<>();
  static{
    DEFAULT_CONVERTER_MAP.putAll(PatternLayout.defaultConverterMap);
    DEFAULT_CONVERTER_MAP.put("highlight", CustomHighlightingCompositeConverter.class.getName());
  }

  public CustomPatternLayout() {
    this.postCompileProcessor = null;
  }

  @Override
  public Map<String, String> getDefaultConverterMap() {
    return DEFAULT_CONVERTER_MAP;
  }

  @Override
  public String doLayout(ILoggingEvent event) {
    return super.doLayout(event);
  }

  @Override
  protected String getPresentationHeaderPrefix() {
    return super.getPresentationHeaderPrefix();
  }
}
