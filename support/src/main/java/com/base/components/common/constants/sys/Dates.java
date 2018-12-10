package com.base.components.common.constants.sys;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.Assert;

/**
 * 日期常量
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version 1.0.0, 2017-07-13 14:08
 */
public abstract class Dates {

  public static final String DATE_TIME_FORMATTER_PATTERN = "yyyy-MM-dd HH:mm:ss";

  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern(DATE_TIME_FORMATTER_PATTERN);

  public static final String DATE_TIME_DOC_EXP = "2017-10-10 10:10:10";

  public static final String YEAR_FORMATTER_PATTERN = "yyyy";

  public static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormat.forPattern(YEAR_FORMATTER_PATTERN);

  public static final String YEAR_DOC_EXP = "2017";

  public static final String DATE_FORMATTER_PATTERN = "yyyy-MM-dd";

  public static final String DATE_RECORD_FORMATTER_PATTERN = "yyyyMMdd";

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern(DATE_FORMATTER_PATTERN);

  public static final String DATE_DOC_EXP = "2017-10-10";

  public static final String DATE_MINUTE_FORMATTER_PATTERN = "yyyy-MM-dd HH:mm";

  public static final DateTimeFormatter DATE_MINUTE_FORMATTER = DateTimeFormat.forPattern(DATE_MINUTE_FORMATTER_PATTERN);

  public static final String DATE_MINUTE_DOC_EXP = "2017-10-10 10:10";

  public static DateTime parse(String src) {
    return parse(src, DATE_TIME_FORMATTER);
  }

  public static DateTime parse(String src, DateTimeFormatter formatter) {
    Assert.hasText(src, "source string is null");
    return DateTime.parse(src, formatter == null ? DATE_TIME_FORMATTER : formatter);
  }

  public static DateTime parse(String src, String formatterPattern) {
    return parse(src,
      StringUtils.isBlank(formatterPattern) ? DATE_TIME_FORMATTER : DateTimeFormat.forPattern(formatterPattern));
  }
}
