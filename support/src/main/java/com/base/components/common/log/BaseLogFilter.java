package com.base.components.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.MDC;

import java.util.Map;
import java.util.function.Supplier;

/**
 * BaseLogFilter
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-08-08 11:01
 */
public class BaseLogFilter extends Filter<ILoggingEvent> {
  private static final String IGNORE_PRINT_KEY = "_IGNORE_PRINT_";
  private Level topLevel;

  @Override
  public FilterReply decide(ILoggingEvent event) {
    //当前日志级别需小于等于 topLevel 才会输出
    if (topLevel != null && topLevel.toInt() > event.getLevel().toInt()) {
      return FilterReply.DENY;
    }
    //判断是否过滤
    Map<String, String> map = event.getMDCPropertyMap();
    if (map != null && Boolean.TRUE.toString().equals(map.get(IGNORE_PRINT_KEY))) {
      return FilterReply.DENY;
    }
    return FilterReply.NEUTRAL;
  }

  public void setTopLevel(Level topLevel) {
    this.topLevel = topLevel;
  }

  /**
   * 设置当前操作忽略打印日志
   * @param supplier 忽略日志打印的操作
   * @return 操作返回值
   */
  public static <T> T ignoreLogging(Supplier<T> supplier){
    try{
      MDC.put(BaseLogFilter.IGNORE_PRINT_KEY, Boolean.TRUE.toString());
      return supplier.get();
    }finally {
      MDC.remove(BaseLogFilter.IGNORE_PRINT_KEY);
    }
  }


}
