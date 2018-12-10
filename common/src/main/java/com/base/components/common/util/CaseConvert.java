package com.base.components.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CaseConvert
 *
 * @author <a href="drakelee1221@gmail.com">LiGeng</a>
 * @version v1.0.0
 * @date 2018-06-28 10:02
 */
public abstract class CaseConvert {
  private static final Pattern UNDERLINE_PATTERN = Pattern.compile("_(\\w)");
  private static final Pattern CAMELCASE_PATTERN = Pattern.compile("[A-Z]");
  private static final String UNDERLINE = "_";

  /**
   * 下划线转驼峰
   * @param src
   *
   * @return
   */
  public static String toCamelCase(String src) {
//    src = src.toLowerCase();
    final StringBuffer sb = new StringBuffer();
    Matcher m = UNDERLINE_PATTERN.matcher(src);
    while (m.find()) {
      m.appendReplacement(sb, m.group(1).toUpperCase());
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * 驼峰转下划线
   * @param src
   *
   * @return
   */
  public static String toUnderline(String src) {
    final StringBuffer sb = new StringBuffer();
    Matcher m = CAMELCASE_PATTERN.matcher(src);
    while (m.find()) {
      m.appendReplacement(sb,UNDERLINE + m.group(0).toLowerCase());
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
