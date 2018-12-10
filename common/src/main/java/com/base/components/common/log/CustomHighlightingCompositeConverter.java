package com.base.components.common.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

import static ch.qos.logback.core.pattern.color.ANSIConstants.*;

/**
 * CustomHighlightingCompositeConverter
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-07-03 11:39
 */
public class CustomHighlightingCompositeConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {
  @Override
  protected String getForegroundColorCode(ILoggingEvent event) {
    Level level = event.getLevel();
    switch (level.toInt()) {
      case Level.ERROR_INT:
        return RED_FG;
      case Level.WARN_INT:
        return YELLOW_FG;
      case Level.INFO_INT:
        //AnsiColor.BRIGHT_BLUE
        return "94";
      default:
        return DEFAULT_FG;
    }
  }
}
